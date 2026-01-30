#include <jni.h>
#include <cstring>
#include "com_microsoft_pict_PictJni.h"
#include "../api/pictapi.h"

//
// Helper macros for handle conversion
//
#define PICT_HANDLE_TO_JLONG(handle) (reinterpret_cast<jlong>(handle))
#define JLONG_TO_PICT_HANDLE(jhandle) (reinterpret_cast<PICT_HANDLE>(jhandle))

//
// JNI Implementation - Task Management
//

JNIEXPORT jlong JNICALL Java_com_microsoft_pict_PictJni_createTask
    (JNIEnv *env, jobject obj)
{
    PICT_HANDLE task = PictCreateTask();
    return PICT_HANDLE_TO_JLONG(task);
}

JNIEXPORT void JNICALL Java_com_microsoft_pict_PictJni_deleteTask
    (JNIEnv *env, jobject obj, jlong task)
{
    PictDeleteTask(JLONG_TO_PICT_HANDLE(task));
}

//
// JNI Implementation - Model Management
//

JNIEXPORT jlong JNICALL Java_com_microsoft_pict_PictJni_createModel
    (JNIEnv *env, jobject obj, jint randomSeed)
{
    PICT_HANDLE model = PictCreateModel(static_cast<unsigned int>(randomSeed));
    return PICT_HANDLE_TO_JLONG(model);
}

JNIEXPORT void JNICALL Java_com_microsoft_pict_PictJni_deleteModel
    (JNIEnv *env, jobject obj, jlong model)
{
    PictDeleteModel(JLONG_TO_PICT_HANDLE(model));
}

JNIEXPORT void JNICALL Java_com_microsoft_pict_PictJni_setRootModel
    (JNIEnv *env, jobject obj, jlong task, jlong model)
{
    PictSetRootModel(
        JLONG_TO_PICT_HANDLE(task),
        JLONG_TO_PICT_HANDLE(model)
    );
}

//
// JNI Implementation - Parameter Management
//

JNIEXPORT jlong JNICALL Java_com_microsoft_pict_PictJni_addParameter
    (JNIEnv *env, jobject obj, jlong model, jlong valueCount, jint order, jintArray valueWeights)
{
    unsigned int* weights = nullptr;
    jsize weightsLength = 0;
    
    if (valueWeights != nullptr)
    {
        weightsLength = env->GetArrayLength(valueWeights);
        jint* weightsArray = env->GetIntArrayElements(valueWeights, nullptr);
        
        if (weightsArray != nullptr)
        {
            weights = new unsigned int[weightsLength];
            for (jsize i = 0; i < weightsLength; i++)
            {
                weights[i] = static_cast<unsigned int>(weightsArray[i]);
            }
            env->ReleaseIntArrayElements(valueWeights, weightsArray, JNI_ABORT);
        }
    }
    
    PICT_HANDLE parameter = PictAddParameter(
        JLONG_TO_PICT_HANDLE(model),
        static_cast<size_t>(valueCount),
        static_cast<unsigned int>(order),
        weights
    );
    
    if (weights != nullptr)
    {
        delete[] weights;
    }
    
    return PICT_HANDLE_TO_JLONG(parameter);
}

JNIEXPORT jint JNICALL Java_com_microsoft_pict_PictJni_attachChildModel
    (JNIEnv *env, jobject obj, jlong modelParent, jlong modelChild, jint order)
{
    PICT_RET_CODE retCode = PictAttachChildModel(
        JLONG_TO_PICT_HANDLE(modelParent),
        JLONG_TO_PICT_HANDLE(modelChild),
        static_cast<unsigned int>(order)
    );
    
    return static_cast<jint>(retCode);
}

//
// JNI Implementation - Exclusions and Seeds
//

JNIEXPORT jint JNICALL Java_com_microsoft_pict_PictJni_addExclusion
    (JNIEnv *env, jobject obj, jlong task, jlongArray parameters, jlongArray valueIndices)
{
    if (parameters == nullptr || valueIndices == nullptr)
    {
        return PICT_GENERATION_ERROR;
    }
    
    jsize length = env->GetArrayLength(parameters);
    if (length != env->GetArrayLength(valueIndices))
    {
        return PICT_GENERATION_ERROR;
    }
    
    jlong* paramArray = env->GetLongArrayElements(parameters, nullptr);
    jlong* valueArray = env->GetLongArrayElements(valueIndices, nullptr);
    
    if (paramArray == nullptr || valueArray == nullptr)
    {
        if (paramArray) env->ReleaseLongArrayElements(parameters, paramArray, JNI_ABORT);
        if (valueArray) env->ReleaseLongArrayElements(valueIndices, valueArray, JNI_ABORT);
        return PICT_OUT_OF_MEMORY;
    }
    
    PICT_EXCLUSION_ITEM* exclusionItems = new PICT_EXCLUSION_ITEM[length];
    for (jsize i = 0; i < length; i++)
    {
        exclusionItems[i].Parameter = JLONG_TO_PICT_HANDLE(paramArray[i]);
        exclusionItems[i].ValueIndex = static_cast<PICT_VALUE>(valueArray[i]);
    }
    
    PICT_RET_CODE retCode = PictAddExclusion(
        JLONG_TO_PICT_HANDLE(task),
        exclusionItems,
        static_cast<size_t>(length)
    );
    
    delete[] exclusionItems;
    env->ReleaseLongArrayElements(parameters, paramArray, JNI_ABORT);
    env->ReleaseLongArrayElements(valueIndices, valueArray, JNI_ABORT);
    
    return static_cast<jint>(retCode);
}

JNIEXPORT jint JNICALL Java_com_microsoft_pict_PictJni_addSeed
    (JNIEnv *env, jobject obj, jlong task, jlongArray parameters, jlongArray valueIndices)
{
    if (parameters == nullptr || valueIndices == nullptr)
    {
        return PICT_GENERATION_ERROR;
    }
    
    jsize length = env->GetArrayLength(parameters);
    if (length != env->GetArrayLength(valueIndices))
    {
        return PICT_GENERATION_ERROR;
    }
    
    jlong* paramArray = env->GetLongArrayElements(parameters, nullptr);
    jlong* valueArray = env->GetLongArrayElements(valueIndices, nullptr);
    
    if (paramArray == nullptr || valueArray == nullptr)
    {
        if (paramArray) env->ReleaseLongArrayElements(parameters, paramArray, JNI_ABORT);
        if (valueArray) env->ReleaseLongArrayElements(valueIndices, valueArray, JNI_ABORT);
        return PICT_OUT_OF_MEMORY;
    }
    
    PICT_SEED_ITEM* seedItems = new PICT_SEED_ITEM[length];
    for (jsize i = 0; i < length; i++)
    {
        seedItems[i].Parameter = JLONG_TO_PICT_HANDLE(paramArray[i]);
        seedItems[i].ValueIndex = static_cast<PICT_VALUE>(valueArray[i]);
    }
    
    PICT_RET_CODE retCode = PictAddSeed(
        JLONG_TO_PICT_HANDLE(task),
        seedItems,
        static_cast<size_t>(length)
    );
    
    delete[] seedItems;
    env->ReleaseLongArrayElements(parameters, paramArray, JNI_ABORT);
    env->ReleaseLongArrayElements(valueIndices, valueArray, JNI_ABORT);
    
    return static_cast<jint>(retCode);
}

//
// JNI Implementation - Generation
//

JNIEXPORT jint JNICALL Java_com_microsoft_pict_PictJni_generate
    (JNIEnv *env, jobject obj, jlong task)
{
    PICT_RET_CODE retCode = PictGenerate(JLONG_TO_PICT_HANDLE(task));
    return static_cast<jint>(retCode);
}

//
// JNI Implementation - Result Handling
//

JNIEXPORT jlong JNICALL Java_com_microsoft_pict_PictJni_allocateResultBuffer
    (JNIEnv *env, jobject obj, jlong task)
{
    PICT_RESULT_ROW buffer = PictAllocateResultBuffer(JLONG_TO_PICT_HANDLE(task));
    return PICT_HANDLE_TO_JLONG(buffer);
}

JNIEXPORT void JNICALL Java_com_microsoft_pict_PictJni_freeResultBuffer
    (JNIEnv *env, jobject obj, jlong resultRow)
{
    PictFreeResultBuffer(reinterpret_cast<PICT_RESULT_ROW>(resultRow));
}

JNIEXPORT void JNICALL Java_com_microsoft_pict_PictJni_resetResultFetching
    (JNIEnv *env, jobject obj, jlong task)
{
    PictResetResultFetching(JLONG_TO_PICT_HANDLE(task));
}

JNIEXPORT jlong JNICALL Java_com_microsoft_pict_PictJni_getNextResultRow
    (JNIEnv *env, jobject obj, jlong task, jlong resultRow, jlongArray output)
{
    PICT_RESULT_ROW buffer = reinterpret_cast<PICT_RESULT_ROW>(resultRow);
    size_t remaining = PictGetNextResultRow(JLONG_TO_PICT_HANDLE(task), buffer);
    
    if (remaining > 0 && output != nullptr)
    {
        size_t paramCount = static_cast<size_t>(PictGetTotalParameterCount(JLONG_TO_PICT_HANDLE(task)));
        jsize arrayLength = env->GetArrayLength(output);
        
        if (arrayLength >= static_cast<jsize>(paramCount))
        {
            jlong* outputArray = env->GetLongArrayElements(output, nullptr);
            if (outputArray != nullptr)
            {
                for (size_t i = 0; i < paramCount; i++)
                {
                    outputArray[i] = static_cast<jlong>(buffer[i]);
                }
                env->ReleaseLongArrayElements(output, outputArray, 0);
            }
        }
    }
    
    return static_cast<jlong>(remaining);
}

JNIEXPORT jlong JNICALL Java_com_microsoft_pict_PictJni_getTotalParameterCount
    (JNIEnv *env, jobject obj, jlong task)
{
    size_t count = PictGetTotalParameterCount(JLONG_TO_PICT_HANDLE(task));
    return static_cast<jlong>(count);
}
