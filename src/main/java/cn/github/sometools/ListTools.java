package cn.github.sometools;

public class ListTools {
    /**
     * 将list切割为指定长度的List集合
     * @param list 原表
     * @param size 分割后长度
     * @param <T> list的对象类
     * @return 分割后的List<List<T>>
     */
    public static <T> java.util.List<java.util.List<T>> partitionList(final java.util.List<T> list, final int size) {
        Integer limit = (list.size() + size - 1) / size;
        java.util.List<java.util.List<T>> splitList = java.util.stream.Stream.iterate(0, n -> n + 1).limit(limit).parallel()
                .map(a -> list.stream().skip(a * size).limit(size).parallel().collect(java.util.stream.Collectors.toList()))
                .collect(java.util.stream.Collectors.toList());
        return splitList;
    }
}
