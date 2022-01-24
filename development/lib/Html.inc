<?php

require_once('for_php7.php');
/*
    テーブル作成クラス

使用方法：
    1.クラスのインスタンス生成
        $t = new Table();
    2.ヘッダの幅を設定する(列の数分数値または%を配列で渡す)
        $t->setWidth(array("100","150","200","250"));
    3.ヘッダの項目文字列を設定(列の数分配列で渡す)
        $t->setHeader(array("項目１","項目２","項目３","項目４"));
    4.テーブルデータを行毎に配列で設定する
        $option = array("align=\"center\"","align=\"center\"","align=\"left\"","align=\"left\"")
        $t->addData(array($data1,$data2,$data3,$data4), $option);
    5.生成したテーブルを出力する
        echo $t->toTable();
    ※
       擬似フレームを表示したい場合は
       setFrameHeightメンバ関数にピクセル単位で数値を渡す
       $t->setFrameHeight(300);
       フレーム表示の場合は
       ヘッダの幅はピクセル単位で設定し、右端を'*'

        $t->setWidth(array("100","150","200","*"));
*/

class Table
{
    var $_width;    //ヘッダの幅
    var $_data;     //データ
    var $_header;   //ヘッダ
    var $_option;   //
    var $_tdOption;   //
    var $_trOption;   //
    var $_frameHeight;  //フレームの高さ

    function Table($header=array(),$width=array(),$option=array()){
        $this->setHeader($header);
        $this->setWidth($width);
        $this->setOption($option);
        $this->_data    = array();
        $this->_tdOption    = array();
        $this->_trOption    = "";
        $this->_frameHeight = "";
    }
    function setWidth($width){
        $this->_width = $width;
    }
    function setHeader($header){
        $this->_header = $header;
    }
    function setOption($option){
        $this->_option = $option;
    }
    function setFrameHeight($height){
        $this->_frameHeight = $height;
    }
    function addData($data, $option="", $trOption=""){
        $this->_data[] = $data;
        if ($option != ""){
            $this->_tdOption[] = $option;
        }
        if ($trOption != ""){
            $this->_trOption[] = $trOption;
        }
    }
    function toTable(){
        //ヘッダ作成
        $header = "<tr class=\"no_search\">\n";
        for($i = 0; $i < sizeof($this->_header); $i++){
            $header .= "<th ";
            if (isset($this->_width[$i])){
                $header .= "width=\"" .$this->_width[$i] ."\" ";
            }
            if (isset($this->_option[$i])){
                $header .= $this->_option[$i];
            }
            $header .= ">" .$this->_header[$i] ."</th>\n";
        }
        $header .= "</tr>\n";
        //リスト作成
        $list = "";
        foreach($this->_data as $key => $val){
            $trOpt = (isset($this->_trOption[$key]))? $this->_trOption[$key] : "";
            if ($val[3] == "TITLE") {
                $list .= "<tr class=\"no_search\">\n";
                for($i = 0; $i < sizeof($val) - 1; $i++){
                    $list .= "<th ";
                    if (isset($this->_width[$i])){
                        $list .= "width=\"" .$this->_width[$i] ."\" ";
                    }
                    if (isset($this->_option[$i])){
                        $list .= $this->_option[$i];
                    }
                    $list .= ">" .$val[$i] ."</th>\n";
                }
                $list .= "</tr>\n";
            } else {
                $list .= "<tr ".$trOpt .">\n";
                for($i = 0; $i < sizeof($val); $i++){
                    $bgcolor = "#ffffff";
                    $opt = (isset($this->_tdOption[$key][$i]))? $this->_tdOption[$key][$i] : "";
                    if (preg_match("/bgcolor=\"(.*)\"/i", $opt, $regs) || preg_match("/bgcolor='(.*)'/i", $opt, $regs)){
                        $bgcolor = $regs[1];
                        $opt = str_replace($regs[0], "", $opt);
                    }
                    $list .= "<td ";
                    if ($key == 0 && isset($this->_width[$i])){
                        $list .= "width=\"" .$this->_width[$i] ."\" ";
                    }
                    if ($opt){
                        $list .= $opt ." ";
                    }
                    $list .= "bgcolor=\"". $bgcolor ."\"";
                    $list .= ">" .$val[$i] ."</td>\n";
                }
                $list .= "</tr>\n";
            }

        }
        $div_s = $div_e = "";
        //擬似フレーム作成
        if (is_numeric($this->_frameHeight)){
            $div_s = "<div style=\"width:100%;height:".$this->_frameHeight .";overflow:auto\">\n";
            $div_e = "</div>";
        }
        //全体を作成
        $html = "";
        $html .= <<<EOP
<table width="100%" border="0" cellspacing="0" cellpadding="0">
EOP;
        //擬似フレーム作成
        if (is_numeric($this->_frameHeight)){
            $html .= <<<EOP
  <tr>
    <td >
      <table width="100%" border="0" cellspacing="0" cellpadding="0">
        <tr class="no_search_line">
          <td>
            <table width="100%" border="0" cellspacing="1" cellpadding="3">
              $header
            </table>
          </td>
        </tr>
      </table>
    </td>
  </tr>
EOP;
            $header = "";
        }
        $html .= <<<EOP
  <tr>
    <td>
    $div_s
      <table width="100%" border="0" cellspacing="0" cellpadding="0">
        <tr class="no_search_line">
          <td>
            <table width="100%" border="0" cellspacing="1" cellpadding="3">
              $header
              $list
            </table>
          </td>
        </tr>
      </table>
    $div_e
    </td>
  </tr>
</table>
EOP;
        return $html;
    }
}
/*
    データ移動リスト作成

使用方法：
    1.クラスのインスタンス生成
        $l = new ListBox();
    2.リストのコントロール名を設定する(省略可　デフォルト'leftlist','rightlist')
        $l->setListName('leftlist','rightlist');
    3.リストのタイトルを設定する
        $l->setListTitle('項目１','項目２');
    4.リストの高さを設定する(省略可　デフォルト２０)
        $l->setListSize(10,10);
    5.左右のリストのデータを設定する
        引数１:表示するデータ
        引数２:コード

        $l->addRightData("コード１",1);

    6.生成したテーブルを出力する
        echo  $l->toList($objForm);

        引数はFormオブジェクト

*/
class Listbox
{
    var $_leftName;
    var $_rightName;
    var $_leftTitle;
    var $_righTitle;
    var $_opt_left;     //リストデータ(左)
    var $_opt_right;    //リストデータ(右)
    var $_leftSize;     //リスト高
    var $_rightSize;    //リスト高

    function Listbox(){
        //リストのコントロール名を設定(デフォルト)
        $this->_leftName    = "leftList";
        $this->_rightName   = "rightList";
        $this->_opt_left    = array();     //リストデータ(左)
        $this->_opt_right   = array();     //リストデータ(右)
        $this->_leftSize    = 20;     //リスト高(デフォルト)
        $this->_rightSize   = 20;     //リスト高(デフォルト)
    }
    //リストのコントロール名を設定
    function setListName($leftname,$rightname){
        $this->_leftName    = $leftname;
        $this->_rightName   = $rightname;
    }
    //リストのタイトルを設定
    function setListTitle($lefttitle,$righttitle){
        $this->_leftTitle   = $lefttitle;
        $this->_rightTitle  = $righttitle;
    }
    //リスト高を設定
    function setListSize($leftSize,$rightSize){
        $this->_leftSize   = $leftSize;
        $this->_rightSize  = $rightSize;
    }
    //リストデータを設定
    function addLeftData($label, $value){
        $this->_opt_left[] = array("label" =>$label,
                                   "value" =>$value
                                   );
    }
    //リストデータを設定
    function addRightData($label, $value){
        $this->_opt_right[] = array("label" =>$label,
                                   "value" =>$value
                                   );
    }
    function toList(&$objForm){
        //左側のリスト
        $objForm->ae( array("type"        => "select",
                            "name"        => $this->_leftName,
                            "size"        => $this->_leftSize,
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right')\"",
                            "options"     => $this->_opt_left));

        //右側のリスト
        $objForm->ae( array("type"        => "select",
                            "name"        => $this->_rightName,
                            "size"        => $this->_rightSize,
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left')\"",
                            "options"     => $this->_opt_right));

        //追加ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return move('sel_add_all');\"" ) );

        //追加ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return move('left');\"" ) );

        //削除ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"return move('right');\"" ) );

        //削除ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"return move('sel_del_all');\"" ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $l_sel = $objForm->ge($this->_leftName);
        $r_sel = $objForm->ge($this->_rightName);
        $sel = $objForm->ge("sel_add_all");
        $sel.= "<br>\n";
        $sel.= $objForm->ge("sel_add");
        $sel.= "<br>\n";
        $sel.= $objForm->ge("sel_del");
        $sel.= "<br>\n";
        $sel.= $objForm->ge("sel_del_all");

        $html = <<<EOP
<script language="JavaScript">
var left = '{$this->_leftName}';
var right = '{$this->_rightName}';

function doSubmit(cmd) {
    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    if (document.forms[0][left].length==0 && document.forms[0][right].length==0) {
        alert('指定範囲が正しく有りません。');
        return false;
    }
    for (var i = 0; i < document.forms[0][left].length; i++)
    {
        attribute3.value = attribute3.value + sep + document.forms[0][left].options[i].value;
        sep = ",";
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function ClearList(OptionList)
{
    OptionList.length = 0;
}
function move(side)
{
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var current1 = 0;
    var current2 = 0;
    var y=0;
    var attribute;

    //assign what select attribute treat as attribute1 and attribute2
    if (side == "right" || side == "sel_del_all")
    {
        attribute1 = document.forms[0][left];
        attribute2 = document.forms[0][right];
    }
    else
    {
        attribute1 = document.forms[0][right];
        attribute2 = document.forms[0][left];
    }
    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++)
    {
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++)
    {
        if (side == "right" || side == "left")
        {
            if ( attribute1.options[i].selected )
            {
                y=current1++
                temp1[y] = attribute1.options[i].value;
                tempa[y] = attribute1.options[i].text;
            }
            else
            {
                y=current2++
                temp2[y] = attribute1.options[i].value;
                tempb[y] = attribute1.options[i].text;
            }
        } else {

            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
        }
    }

    //sort
    temp1 = temp1.sort();
    tempa = tempa.sort();

    //generating new options
    for (var i = 0; i < temp1.length; i++)
    {
        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[i];
        attribute2.options[i].text =  tempa[i];
    }

    //generating new options
    ClearList(attribute1);
    if (temp2.length>0)
    {
        for (var i = 0; i < temp2.length; i++)
        {
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text =  tempb[i];
        }
    }

    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0][left].length; i++)
    {
        attribute3.value = attribute3.value + sep + document.forms[0][left].options[i].value;
        sep = ",";
    }
}
</script>
<table width="100%" border="0" cellpadding="0" cellspacing="1">
  <tr bgcolor="#ffffff">
    <td>
      <table width="100%" border="0" cellpadding="0" cellspacing="10" >
        <tr class="no_search_line">
          <td width="100%">
            <table width="100%" border="0" cellpadding="0" cellspacing="1">
              <tr align="center" class="no_search">
                <th><font color="white">{$this->_leftTitle}</font></th>
                <th class="no_search">　</th>
                <th><font color="white">{$this->_rightTitle}</font></th>
              </tr>
              <tr align="center" bgcolor="#ffffff">
                <td width="45%">$l_sel</td>
                <td width="*%" class="no_search">$sel</td>
                <td width="45%">$r_sel</td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </td>
  </tr>
</table>
EOP;
        return $html;
    }

}

?>