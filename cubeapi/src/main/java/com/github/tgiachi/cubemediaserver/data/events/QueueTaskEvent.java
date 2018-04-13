package com.github.tgiachi.cubemediaserver.data.events;

import com.github.tgiachi.cubemediaserver.data.QueueTaskObject;
import lombok.Data;

@Data
public class QueueTaskEvent {
    QueueTaskObject queueTaskObject;
}
