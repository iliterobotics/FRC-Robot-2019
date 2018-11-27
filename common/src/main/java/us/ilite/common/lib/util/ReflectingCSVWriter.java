package us.ilite.common.lib.util;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Writes data to a CSV file
 */
public class ReflectingCSVWriter<T> {
    ConcurrentLinkedDeque<String> mLinesToWrite = new ConcurrentLinkedDeque<>();
    PrintWriter mOutput = null;
    Class<T> typeClass;
    Field[] mFields;

    public ReflectingCSVWriter(String fileName, Class<T> typeClass) {
        this.typeClass = typeClass;
        mFields = typeClass.getFields();
        try {
            mOutput = new PrintWriter(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // Write field names.
        StringBuffer line = new StringBuffer();
        for (Field field : mFields) {
            if (line.length() != 0) {
                line.append(", ");
            }
            line.append(field.getName());
        }
        writeLine(line.toString());
    }

    public void add(T value) {
        StringBuffer line = new StringBuffer();

        // If the class has no fields, attempt to use its own toCSV()
        if(mFields.length <= 0) {
            try {
                Method method = value.getClass().getMethod("toCSV");
                line.append(method.invoke(value));
            } catch (NoSuchMethodException pE) {
                pE.printStackTrace();
            } catch (InvocationTargetException pE) {
                pE.printStackTrace();
            } catch (IllegalAccessException pE) {
                pE.printStackTrace();
            }
        }

        for (Field field : mFields) {
            if (line.length() != 0) {
                line.append(", ");
            }
            try {
                if (CSVWritable.class.isAssignableFrom(field.getType())) {
                    line.append(((CSVWritable) field.get(value)).toCSV());
                } else {
                    line.append(field.get(value).toString());
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        mLinesToWrite.add(line.toString());
    }

    protected synchronized void writeLine(String line) {
        if (mOutput != null) {
            mOutput.println(line);
        }
    }

    // Call this periodically from any thread to write to disk.
    public void write() {
        while (true) {
            String val = mLinesToWrite.pollFirst();
            if (val == null) {
                break;
            }
            writeLine(val);
        }
    }

    public synchronized void flush() {
        if (mOutput != null) {
            write();
            mOutput.flush();
        }
    }
}
