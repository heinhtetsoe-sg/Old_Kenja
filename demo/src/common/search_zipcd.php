<?php

require_once('for_php7.php');

//郵便番号検索
class SearchZipcd
{
    var $form;
    var $addrname;
    var $zipname;
    var $zip;
    var $addr;
    var $frame;         //フレーム名
    var $cmd;           //コマンド
    
    function SearchZipcd(){
        $this->form = new form();
    }
    //住所リスト作成
    function getZipList(){
        $opt = array();
        if ($this->zip || $this->addr){
            $db = Query::dbCheckOut();
            $query = "";
            $query .= "SELECT ";
            $query .= "    ZIPNO, ";
            $query .= "    NEW_ZIPCD, ";
            $query .= "    OLD_ZIPCD, ";
            $query .= "    PREF, ";
            $query .= "    CITY, ";
            $query .= "    CITY_KANA, ";
            $query .= "    TOWN, ";
            $query .= "    TOWN_KANA ";
            $query .= "FROM ";
            $query .= "    ZIPCD_MST ";
            $query .= "WHERE NEW_ZIPCD <> ''";
            if ($this->cmd == "apply"){    //確定ボタン
                $query .= " AND NEW_ZIPCD = '" .$this->zip ."' ";
            }else if ($this->cmd == "search"){    //確定ボタン
                if (isset($this->zip)){
                    $query .= " AND NEW_ZIPCD like '" .$this->zip ."%' ";
                }
                if (isset($this->addr)){
                    $query .= " AND PREF || CITY || TOWN like '%" .$this->addr ."%' ";
                }
            }
            $query .= "ORDER BY NEW_ZIPCD ";
            //教科、科目、クラス取得
            $result = $db->query($query);

            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt[] = array("label" => sprintf("%s　|　%s", $row["NEW_ZIPCD"], $row["PREF"] .$row["CITY"] .$row["TOWN"]),
                                "value" => $row["NEW_ZIPCD"]
                                );
            }
            Query::dbCheckIn($db);    
        }
        $this->form->ae( array("type"       => "select",
                            "name"       => "ZIPLIST",
                            "size"       => "10",
                            "value"      => "",
                            "extrahtml"   => "onclick=\"setAddrText(this);\" ondblclick=\"apply_address(this, '$this->addrname', '$this->zipname');\" STYLE=\"WIDTH:100%\" WIDTH=\"100%\"",
                            "options"    => (is_array($opt))? $opt : array()));

        return $this->form->ge("ZIPLIST");
    }
    function main($rq){
        //テキストボックスの名前
        if ($rq["addrname"]){
            $this->addrname   = $rq["addrname"];
        }
        if ($rq["zipname"]){
            $this->zipname    = $rq["zipname"];
        }
        $this->zip        = $rq["zip"];
        $this->addr       = $rq["addr"];

        if ($rq["frame"]){
            $this->frame      = $rq["frame"];      //フレーム名
        }
        $this->cmd        = $rq["cmd"];      //コマンド
    }
    //住所テキストボックス
    function getTextAddress(){
        //住所
        $this->form->ae( array("type"        => "text",
                            "name"        => "addr",
                            "size"        => 40,
                            "extrahtml"   => " STYLE=\"ime-mode: active\" ",
                            "value"       => $this->addr ));

        return $this->form->ge("addr");
    }

    //郵便番号テキストボックス
    function getTextZip(){
        //郵便番号
        $this->form->ae( array("type"        => "text",
                            "name"        => "zip",
                            "size"        => 10,
                            "maxlength"   => 8,
                            "extrahtml"   => " STYLE=\"ime-mode: inactive\" onblur=\"isZipcd(this)\"",
                            "value"       => $this->zip ));

        return $this->form->ge("zip");
    }
    //検索ボタン作成
    function getBtnSearch(){
        //検索ボタンを作成する
        $this->form->ae( array("type" => "button",
                            "name"        => "search",
                            "value"       => "検 索",
                            "extrahtml"   => "onclick=\"return btn_submit('search')\"" ) );

        return $this->form->ge("search");
    }    
    //反映ボタン作成
    function getBtnApply(){
        //検索ボタンを作成する
        $this->form->ae( array("type" => "button",
                            "name"        => "apply",
                            "value"       => "反 映",
                            "extrahtml"   => "onClick=\"apply_address(document.forms[0].ZIPLIST, '$this->addrname', '$this->zipname');\"" ) );

        $button["apply"] = $this->form->ge("apply");
    }
    //フォームタブ(開始)
    function getStart(){
        return $this->form->get_start("zip", "POST", "search_zipcd.php", "", "zip");
    }
    //フォームタブ(終了)
    function getFinish(){
        return $this->form->get_finish();
    }
    //フレーム取得
    function getFrame(){
        return $this->frame;
    }
    function getZipName(){
        return $this->zipname;
    }
    function getAddrName(){
        return $this->addrname;
    }
}
if ($_GET["cmd"] == "apply"){
    $objZip = new SearchZipcd();
    $objZip->main($_REQUEST);
}else{
    if ($_REQUEST["ZIP_SESSID"] == "") exit;
    $sess = new APP_Session($_REQUEST["ZIP_SESSID"], 'ZIP');
    if (!$sess->isCached($_REQUEST["ZIP_SESSID"], 'ZIP')) {
        $sess->data = new SearchZipcd();
    }
    $sess->data->main($_REQUEST);

    $objZip = $sess->getData();
}
?>
<html>
<head>
<title>郵便番号入力支援</title>
<meta http-equiv="Content-Type" content="text/html; charset=<?php echo CHARSET ?>">
<link rel="stylesheet" href="gk.css">
<script language="JavaScript">
<!--
var  f = <?php echo $objZip->getFrame() ?>;

function setAddrText(obj){
    try{
        var val = obj.options[obj.selectedIndex].text;    
        var arr = val.split("　|　");
        document.forms[0].zip.value = arr[0];
        document.forms[0].addr.value = arr[1];
    }catch(e){
    }
}
// 郵便番号チェック
function isZipcd(obj) {
    txt = obj.value;
    if (txt != ""){
        data = txt.match(/^\d{3}-\d{4}$|^\d{3}-\d{2}$|^\d{3}$/);
        if(!data){
            alert("郵便番号が不正です");
            obj.value = obj.defaultValue;
            return false;
        }
    }else{
        return false;
    }
    return true;    
}

function apply_address(obj, addrname, zipname) {
    if(obj.selectedIndex >= 0){
        var val = obj.options[obj.selectedIndex].text;    
        var arr = val.split("　|　");
        f.document.forms[0][zipname].value = arr[0];
        f.document.forms[0][addrname].value = arr[1];
        f.document.forms[0][zipname].focus();
        f.closeit();
    }else{
        alert("住所が選択されていません");
    }
 
}
function btn_submit(cmd){
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
// -->
</script>
</head>
<body bgcolor="#ffffff" text="#000000" leftmargin="0" topmargin="0" marginwidth="5" marginheight="5"
link="#006633" vlink="#006633" alink="#006633">
<?php echo $objZip->getStart() ?>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td >
      <table width="100%" border="0" cellspacing="0" cellpadding="0">
        <tr class="no_search_line">
          <td>
            <table width="100%" border="0" cellspacing="1" cellpadding="1">
              <tr class="no_search">
                <th width="15%">〒</th>
                <td bgcolor="#ffffff"><?php echo $objZip->getTextZip() ?></td>
              </tr>
              <tr class="no_search">
                <th width="15%">住所</th>
                <td bgcolor="#ffffff"><?php echo $objZip->getTextAddress() ?></td>
              </tr>
              <tr class="no_search">
                <td colspan="2"  bgcolor="#ffffff"><?php echo $objZip->getBtnSearch() ?></td>
              </tr>
              <tr class="no_search">
                <td colspan="2" bgcolor="#ffffff"><?php echo $objZip->getZipList() ."<BR>" .$objZip->getBtnApply() ?></td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </td>
  </tr>
</table>
<input type="hidden" name="ZIP_SESSID" value="<?php echo $_REQUEST["ZIP_SESSID"] ?>">
<input type="hidden" name="cmd">
<?php echo $objZip->getFinish() ?>
<script language="JavaScript">
    if (document.forms[0].zip.value == '' && document.forms[0].addr.value == ''){
        document.forms[0].zip.value = f.document.forms[0]['<?php echo $objZip->getZipName() ?>'].value;
        document.forms[0].addr.value = f.document.forms[0]['<?php echo $objZip->getAddrName() ?>'].value;
    }
</script>
</body>
</html>
<?php 
if ($_GET["cmd"] == "apply"){
?>
<script language="JavaScript">
    if (document.forms[0].ZIPLIST.length == 1){
        var val = document.forms[0].ZIPLIST.options[0].text;
        var arr = val.split("　|　");
        f.document.forms[0]['<?php echo $objZip->getZipName() ?>'].value = arr[0];
        f.document.forms[0]['<?php echo $objZip->getAddrName() ?>'].value = arr[1];
    }else if(document.forms[0].ZIPLIST.length > 1){//同一郵便番号で複数地域ありの場合メッセージ変更
        alert("住所が複数存在します。入力支援ボタンを利用してください。");
    }else{
        alert("住所が見つかりませんでした。");
    }
</script>
<?php
}
?>