package cz.sejsel

import com.dylibso.chicory.runtime.GlobalInstance
import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.runtime.TableInstance

fun Instance.getGlobals(): List<GlobalInstance> {
    // There is a private field: private final GlobalInstance[] globals;
    // and public GlobalInstance global(int index), but no way to get the globals count and it mixes in imported globals

    val field = Instance::class.java.getDeclaredField("globals")
    field.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    return (field.get(this) as Array<GlobalInstance>).toList()
}

fun Instance.getTables(): List<TableInstance> {
    // There is a private field: private final TableInstance[] tables;
    // and public TableInstance table(int index), but no way to get the tables count

    val field = Instance::class.java.getDeclaredField("tables")
    field.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    return (field.get(this) as Array<TableInstance>).toList()
}