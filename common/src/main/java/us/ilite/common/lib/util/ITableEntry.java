
package us.ilite.common.lib.util;
public interface ITableEntry {

    public String getString(String key);

    public boolean setString(String newString);

    public boolean exists();
    
}