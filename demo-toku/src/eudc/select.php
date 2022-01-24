<?php

  require_once('for_php7.php');

  $page = isset($_GET["page"]) ? $_GET["page"] : "0";
  $code = array();
  $path = 'eudc_'.$page;
  if (!file_exists($path)) {
    $page = "0";
    $path = 'eudc_'.$page;
  }
  $start = 57344 + intval($page) * 256;	//E000
  $eudc = file_get_contents($path);
  for($i=0;$i<256;$i++) {
      $C = mb_substr($eudc, $i, 1, 'utf8');
      $code[] = $C;
  }
  $scope = sprintf("%04X～%04X", $start, $start+255);

print <<<EOD

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="viewport" content="width=device-width">
<title>外字一覧</title>

<link rel="stylesheet" type="text/css" href="select.css" />

<script type="text/javascript" src="../jquery-3.3.1.min.js"></script>
<!-- script type="text/javascript" src="http://www.google.com/jsapi"></script>
<script type="text/javascript">google.load("jquery", "1.7");</script -->
<script type="text/javascript">

window.onload = function() {
  var page = parseInt(document.forms.formID.pageID.value, 10);
  if (page > 0) {
    document.getElementById("before").disabled = "";
  }
  else {
    document.getElementById("before").disabled = "disabled";
  }
};

$(function() {
	//セルの選択
    $('td').on('click', function() {
//alert("a");
        selectDomElm(this);
        document.execCommand('copy');
	});
});

//領域選択
function selectDomElm(obj){
  // Rangeオブジェクトの取得
  var range = document.createRange();
  // 範囲の指定
  range.selectNodeContents(obj);

　// Selectionオブジェクトを返す。ユーザが選択した範囲が格納されている
  var selection = window.getSelection();
  // 選択をすべてクリア
  selection.removeAllRanges();
　// 新規の範囲を選択に指定
  selection.addRange(range);
}

function nextPage() {
  var page = parseInt(document.forms.formID.pageID.value, 10) + 1;
  document.forms.formID.pageID.value = page.toString();
  document.pageForm.submit();
}

function beforePage() {
  var page = parseInt(document.forms.formID.pageID.value, 10);
  if (page > 0)
      page--;
  document.forms.formID.pageID.value = page.toString();
  document.pageForm.submit();
}

</script>
</head>

<body>

<form name="pageForm" id="formID" action="" method="get">
<font size="4"><B>コード [$scope]</B></font>
　<input type=button value="次コード" onClick="nextPage();">
 <input type=button id="before" value="前コード" onClick="beforePage();">
 <input type=hidden name="page" id="pageID" value=$page>
</form>

<table class="sample" border="1">
<tr>
 <td>$code[0]</td><td>$code[1]</td><td>$code[2]</td><td>$code[3]</td><td>$code[4]</td><td>$code[5]</td><td>$code[6]</td><td>$code[7]</td><td>$code[8]</td><td>$code[9]</td><td>$code[10]</td><td>$code[11]</td><td>$code[12]</td><td>$code[13]</td><td>$code[14]</td><td>$code[15]</td>
</tr>
<tr>
 <td>$code[16]</td><td>$code[17]</td><td>$code[18]</td><td>$code[19]</td><td>$code[20]</td><td>$code[21]</td><td>$code[22]</td><td>$code[23]</td><td>$code[24]</td><td>$code[25]</td><td>$code[26]</td><td>$code[27]</td><td>$code[28]</td><td>$code[29]</td><td>$code[30]</td><td>$code[31]</td>
</tr>
<tr>
 <td>$code[32]</td><td>$code[33]</td><td>$code[34]</td><td>$code[35]</td><td>$code[36]</td><td>$code[37]</td><td>$code[38]</td><td>$code[39]</td><td>$code[40]</td><td>$code[41]</td><td>$code[42]</td><td>$code[43]</td><td>$code[44]</td><td>$code[45]</td><td>$code[46]</td><td>$code[47]</td>
</tr>
<tr>
 <td>$code[48]</td><td>$code[49]</td><td>$code[50]</td><td>$code[51]</td><td>$code[52]</td><td>$code[53]</td><td>$code[54]</td><td>$code[55]</td><td>$code[56]</td><td>$code[57]</td><td>$code[58]</td><td>$code[59]</td><td>$code[60]</td><td>$code[61]</td><td>$code[62]</td><td>$code[63]</td>
</tr>
<tr>
 <td>$code[64]</td><td>$code[65]</td><td>$code[66]</td><td>$code[67]</td><td>$code[68]</td><td>$code[69]</td><td>$code[70]</td><td>$code[71]</td><td>$code[72]</td><td>$code[73]</td><td>$code[74]</td><td>$code[75]</td><td>$code[76]</td><td>$code[77]</td><td>$code[78]</td><td>$code[79]</td>
</tr>
<tr>
 <td>$code[80]</td><td>$code[81]</td><td>$code[82]</td><td>$code[83]</td><td>$code[84]</td><td>$code[85]</td><td>$code[86]</td><td>$code[87]</td><td>$code[88]</td><td>$code[89]</td><td>$code[90]</td><td>$code[91]</td><td>$code[92]</td><td>$code[93]</td><td>$code[94]</td><td>$code[95]</td>
</tr>
<tr>
 <td>$code[96]</td><td>$code[97]</td><td>$code[98]</td><td>$code[99]</td><td>$code[100]</td><td>$code[101]</td><td>$code[102]</td><td>$code[103]</td><td>$code[104]</td><td>$code[105]</td><td>$code[106]</td><td>$code[107]</td><td>$code[108]</td><td>$code[109]</td><td>$code[110]</td><td>$code[111]</td>
</tr>
<tr>
 <td>$code[112]</td><td>$code[113]</td><td>$code[114]</td><td>$code[115]</td><td>$code[116]</td><td>$code[117]</td><td>$code[118]</td><td>$code[119]</td><td>$code[120]</td><td>$code[121]</td><td>$code[122]</td><td>$code[123]</td><td>$code[124]</td><td>$code[125]</td><td>$code[126]</td><td>$code[127]</td>
</tr>
<tr>
 <td>$code[128]</td><td>$code[129]</td><td>$code[130]</td><td>$code[131]</td><td>$code[132]</td><td>$code[133]</td><td>$code[134]</td><td>$code[135]</td><td>$code[136]</td><td>$code[137]</td><td>$code[138]</td><td>$code[139]</td><td>$code[140]</td><td>$code[141]</td><td>$code[142]</td><td>$code[143]</td>
</tr>
<tr>
 <td>$code[144]</td><td>$code[145]</td><td>$code[146]</td><td>$code[147]</td><td>$code[148]</td><td>$code[149]</td><td>$code[150]</td><td>$code[151]</td><td>$code[152]</td><td>$code[153]</td><td>$code[154]</td><td>$code[155]</td><td>$code[156]</td><td>$code[157]</td><td>$code[158]</td><td>$code[159]</td>
</tr>
<tr>
 <td>$code[160]</td><td>$code[161]</td><td>$code[162]</td><td>$code[163]</td><td>$code[164]</td><td>$code[165]</td><td>$code[166]</td><td>$code[167]</td><td>$code[168]</td><td>$code[169]</td><td>$code[170]</td><td>$code[171]</td><td>$code[172]</td><td>$code[173]</td><td>$code[174]</td><td>$code[175]</td>
</tr>
<tr>
 <td>$code[176]</td><td>$code[177]</td><td>$code[178]</td><td>$code[179]</td><td>$code[180]</td><td>$code[181]</td><td>$code[182]</td><td>$code[183]</td><td>$code[184]</td><td>$code[185]</td><td>$code[186]</td><td>$code[187]</td><td>$code[188]</td><td>$code[189]</td><td>$code[190]</td><td>$code[191]</td>
</tr>
<tr>
 <td>$code[192]</td><td>$code[193]</td><td>$code[194]</td><td>$code[195]</td><td>$code[196]</td><td>$code[197]</td><td>$code[198]</td><td>$code[199]</td><td>$code[200]</td><td>$code[201]</td><td>$code[202]</td><td>$code[203]</td><td>$code[204]</td><td>$code[205]</td><td>$code[206]</td><td>$code[207]</td>
</tr>
<tr>
 <td>$code[208]</td><td>$code[209]</td><td>$code[210]</td><td>$code[211]</td><td>$code[212]</td><td>$code[213]</td><td>$code[214]</td><td>$code[215]</td><td>$code[216]</td><td>$code[217]</td><td>$code[218]</td><td>$code[219]</td><td>$code[220]</td><td>$code[221]</td><td>$code[222]</td><td>$code[223]</td>
</tr>
<tr>
 <td>$code[224]</td><td>$code[225]</td><td>$code[226]</td><td>$code[227]</td><td>$code[228]</td><td>$code[229]</td><td>$code[230]</td><td>$code[231]</td><td>$code[232]</td><td>$code[233]</td><td>$code[234]</td><td>$code[235]</td><td>$code[236]</td><td>$code[237]</td><td>$code[238]</td><td>$code[239]</td>
</tr>
<tr>
 <td>$code[240]</td><td>$code[241]</td><td>$code[242]</td><td>$code[243]</td><td>$code[244]</td><td>$code[245]</td><td>$code[246]</td><td>$code[247]</td><td>$code[248]</td><td>$code[249]</td><td>$code[250]</td><td>$code[251]</td><td>$code[252]</td><td>$code[253]</td><td>$code[254]</td><td>$code[255]</td>
</tr>
</table>

</body>
</html>
EOD;
?>
