package io.yimin.chrysanthemum.hazelcast.api.task;

import java.io.IOException;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class ExecutorParam implements DataSerializable {

  protected String executorClassName;


  @Override
  public void writeData(ObjectDataOutput objectDataOutput) throws IOException {
    objectDataOutput.writeUTF(this.executorClassName);
  }

  @Override
  public void readData(ObjectDataInput objectDataInput) throws IOException {
    this.executorClassName = objectDataInput.readUTF();
  }
}
