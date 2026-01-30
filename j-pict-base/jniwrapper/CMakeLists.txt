set(target_name
  pict_jni
)

set(pict_jni_output
  pictjni
)

# Find JNI
find_package(JNI REQUIRED)
include_directories(${JNI_INCLUDE_DIRS})

set(pict_jni_inc
  ${CMAKE_CURRENT_SOURCE_DIR}/com_microsoft_pict_PictJni.h
  ${CMAKE_CURRENT_SOURCE_DIR}/resource.h
)

set(pict_jni_src
  ${CMAKE_CURRENT_SOURCE_DIR}/jniwrapper.cpp
  $<$<BOOL:${WIN32}>:${CMAKE_CURRENT_SOURCE_DIR}/Resource.rc>
  $<$<BOOL:${WIN32}>:${CMAKE_CURRENT_SOURCE_DIR}/pictjni.def>
)

add_library(${target_name}
  SHARED
    ${pict_jni_inc}
    ${pict_jni_src}
)

add_compile_definitions(${target_name}
  PRIVATE
    PICTJNI_EXPORTS
)

source_group(TREE ${CMAKE_CURRENT_SOURCE_DIR}
  FILES
    ${pict_jni_inc}
    ${pict_jni_src}
)

target_link_libraries(${target_name}
  PRIVATE
    PictBuildOptions
    pict_api
    ${JNI_LIBRARIES}
)

set_target_properties(${target_name}
  PROPERTIES
    FOLDER src
    OUTPUT_NAME ${pict_jni_output}
)
