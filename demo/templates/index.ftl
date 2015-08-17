<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>freeMarker 测试页面</title>
</head>
<body>
<#if files??>
<#list files as file>
${file.view}
</#list>
<#else>
<span style='color:red'>Empty!</span>
</#if>
</body>
</html>