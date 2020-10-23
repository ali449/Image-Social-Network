package com.example.shediz.data.network


class Resource<out T>(val status: Status, val data: T?, val pageData: Int?, val error: Throwable?)
{
    companion object
    {
        fun <T> success(data: T, page: Int? = null) = Resource(Status.SUCCESS, data, page, null)

        fun <T> error(error: Throwable, data: T? = null, page: Int? = null) = Resource(Status.ERROR, data, page, error)

        fun <T> loading(data: T? = null, page: Int? = null) = Resource(Status.LOADING, data, page,null)
    }
}

enum class Status
{
    SUCCESS,
    ERROR,
    LOADING;

    /**
     * Returns `true` if the [Status] is success else `false`.
     */
    fun isSuccessful() = this == SUCCESS

    /**
     * Returns `true` if the [Status] is loading else `false`.
     */
    fun isLoading() = this == LOADING

    /**
     * Returns `true` if the [Status] is in error else `false`.
     */
    fun isError() = this == ERROR
}