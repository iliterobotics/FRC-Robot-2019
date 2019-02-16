package us.ilite.common.config;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Class that contains utility methods for all things System Settings
 */
public abstract class AbstractSystemSettingsUtils {
        /**
     * Touch-file to indicate whether the practice bot constants should be loaded.
     */
    private static final File PRACTICE_BOT_FILE = new File(
            System.getProperty("user.home") + File.separator + "practicebot");

    public static void loadPracticeSettings(SystemSettings sysSettings) {
        if(PRACTICE_BOT_FILE.exists()) {
            copyOverValues(PracticeBotSystemSettings.getInstance(), sysSettings);
        }
    }
    /**
     * This method will take all of the static fields in this class and apply the values to the 
     * passed in object's static fields that have the exact field name. 
     * @param destinationObject 
     *  The object that will have it's static fields updated with the fields in the from object
     */
    public static void copyOverValues(Object fromObject, Object destinationObject) {
        if(fromObject == null || destinationObject == null) {
            return;
        }
        Field [] allFields = fromObject.getClass().getFields();
       
        Map<String,Object>baseClassMap =  
            Arrays.stream(allFields).filter(aField->Modifier.isStatic(aField.getModifiers())).collect(Collectors.toMap(aField->aField.getName(), aField->{
                
                try {
                    return aField.get(destinationObject);
                } catch(Exception e) {
                    System.err.println("Unable to get field: " + aField.getName() +" from obj: " + destinationObject.getClass());
                    e.printStackTrace();
                    return null;
                }
            }
            ));
        baseClassMap.forEach((key,val)->System.out.println(key+", " + val));

        Arrays.stream(destinationObject.getClass().getFields()).filter(aField->Modifier.isStatic(aField.getModifiers())).forEach(aField->{
            Object value  = baseClassMap.get(aField.getName());
            if(value != null) {
                try {
                    System.out.println("Setting field: " + aField.getName() +" to: " + value);
                    aField.set(destinationObject, value);
                } catch(Exception e) {
                    System.err.println("Unable to set the field: " + aField.getName()+" on object: "+ destinationObject.getClass());
                    e.printStackTrace();
                }
            }
        });
    }

    public static Map<String,String> getAllCurrentPropsAndValues(Object obj) { 
        return Arrays.stream(obj.getClass().getFields()).filter(aField->Modifier.isStatic(
            aField.getModifiers())).collect(
            Collectors.toMap((aField)->aField.getName(),(aField)->{
                Object returnObj = "";
                try {
                 returnObj = aField.get(obj);
                if(returnObj == null) {
                    returnObj = "null";
                }
            } catch(Exception e) {
                returnObj = "unk";
            }
                return returnObj.toString();
            }));
    }

    /**
     * Private constructor to prevent instantiation
     */
    private AbstractSystemSettingsUtils() {
    }
}
