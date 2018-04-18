package com.github.tgiachi.cubemediaserver.mediaparsers;

import com.github.tgiachi.cubemediaserver.annotations.MediaParser;
import com.github.tgiachi.cubemediaserver.data.events.ffmpeg.MediaInfoEvent;
import com.github.tgiachi.cubemediaserver.data.events.media.InputMediaFileEvent;
import com.github.tgiachi.cubemediaserver.data.media.ParsedMediaObject;
import com.github.tgiachi.cubemediaserver.entities.MediaFileTypeEnum;
import com.github.tgiachi.cubemediaserver.entities.MovieEntity;
import com.github.tgiachi.cubemediaserver.interfaces.mediaparser.IMediaParser;
import com.github.tgiachi.cubemediaserver.interfaces.services.IFFMpegService;
import com.github.tgiachi.cubemediaserver.repositories.MoviesRepository;
import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.AsyncResult;

import javax.annotation.PostConstruct;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@MediaParser(extensions = {"avi", "mkv"})
public class VideoMediaParser implements IMediaParser {

    @Value("${moviedb.apikey}")
    public String movieDbApiKey;

    private final Logger mLogger = LoggerFactory.getLogger(VideoMediaParser.class);

    private TmdbApi mTmdbApi;

    @Autowired
    public MoviesRepository moviesRepository;

    @Autowired
    public IFFMpegService iffMpegService;


    final String regex = "^\n"
            + "(?<title> \n"
            + "  [-\\w'\\\"]+\n"
            + "  (?<separator> [ .] ) \n"
            + "  (?: [-\\w'\\\"]+\\2 )*?\n"
            + ")\n"
            + "(?:\n"
            + "  (?:\n"
            + "    (?! \\d+ \\2 )\n"
            + "    (?: s (?: eason \\2? )? )?\n"
            + "    (?<season> \\d\\d? )\n"
            + "    (?: e\\d\\d? (?:-e?\\d\\d?)? | x\\d\\d? )? |\n"
            + "    (?<year> [(\\]]?\\d{4}[)\\]]? ) \n"
            + "  )\n"
            + "  (?=\\2) |\n"
            + "  (?= BOXSET  | XVID   | DIVX | LIMITED   | \n"
            + "      UNRATED | PROPER | DTS  | AC3 | AAC | BLU[ -]?RAY | \n"
            + "      HD(?:TV|DVD) | (?:DVD|B[DR]|WEB)RIP | \\d+p |Â [hx]\\.?264\n"
            + "  )\n"
            + ")";


    final Pattern mMoviePattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.COMMENTS);


    @PostConstruct
    public void init() {
        mTmdbApi = new TmdbApi(movieDbApiKey);

    }

    @Override
    public Future<ParsedMediaObject> Parse(InputMediaFileEvent inputMediaFileEvent) {

        AsyncResult<ParsedMediaObject> out = new AsyncResult<>(null);

        if (inputMediaFileEvent.getMediaType() == MediaFileTypeEnum.MOVIE)
            out = new AsyncResult<>(scanMovie(inputMediaFileEvent));

        if (inputMediaFileEvent.getMediaType() == MediaFileTypeEnum.TVSHOW)
            out = new AsyncResult<>(scanTvSeries(inputMediaFileEvent));

        return out.completable();
    }

    private ParsedMediaObject scanMovie(InputMediaFileEvent inputMediaFileEvent) {

        final Matcher matcher = mMoviePattern.matcher(inputMediaFileEvent.getFilename());

        ParsedMediaObject out = new ParsedMediaObject();
        out.setMediaType(inputMediaFileEvent.getMediaType());
        out.setFilename(inputMediaFileEvent.getFullPathFileName());


        if (matcher.find()) {
            String title = matcher.group("title");
            String separator = matcher.group("separator");
            String year = matcher.group("year");

            if (separator != null) {
                title = title.replace(separator, " ");
            }


            MediaInfoEvent result = iffMpegService.getMediaInformation(inputMediaFileEvent.getFullPathFileName());

            mLogger.info("{} HW: {}x{} => Duration: {} seconds,  Codec: {} ", inputMediaFileEvent.getFilename(), result.getWidth(), result.getHeight(), result.getDuration(), result.getCodec());


            Integer yearInt = year != "" ? Integer.parseInt(year) : null;

            MovieResultsPage resultsPage = mTmdbApi.getSearch().searchMovie(title, yearInt, "it", true, 1);

            if (resultsPage.getTotalPages() > 0) {
                mLogger.info("Found on TvMovieDb correlation between {} -> {}", inputMediaFileEvent.getFilename(), resultsPage.getResults().get(0).getTitle());

                MovieDb movie = resultsPage.getResults().get(0);

                String mediaId = saveMovieOnDb(movie, inputMediaFileEvent, result.getWidth(), result.getHeight());

                out.setMediaId(mediaId);

                out.setSavedOnDb(true);

            }
        }
        return out;

    }

    private String saveMovieOnDb(MovieDb movie, InputMediaFileEvent inputMediaFileEvent, int width, int height) {
        if (moviesRepository.findByTitle(movie.getTitle()) == null) {
            MovieEntity movieEntity = new MovieEntity();

            movieEntity.setTitle(movie.getTitle());
            movieEntity.setFilename(inputMediaFileEvent.getFullPathFileName());
            movieEntity.setDirectoryEntry(inputMediaFileEvent.getDirectoryEntry().getDirectory());
            movieEntity.setMovieDbId(movie.getId());
            movieEntity.setWidth(width);
            movieEntity.setHeight(height);

            moviesRepository.save(movieEntity);

            return movieEntity.getUid();
        }

        return "";
    }

    private ParsedMediaObject scanTvSeries(InputMediaFileEvent inputMediaFileEvent) {
        return null;
    }
}
