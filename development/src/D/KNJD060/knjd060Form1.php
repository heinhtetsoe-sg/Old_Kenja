<?php

require_once('for_php7.php');

/********************************************************************/
/* 変更履歴                                                         */
/* ･NO001：学年末指定した場合、今学期のデータを使用 山城 2004/10/26 */
/* ･NO002：テスト項目の設定をDBからでなく、直接設定 山城 2004/10/26 */
/* ･NO003：チェックボックス追加                     山城 2004/11/25 */
/* ･NO004：日付変更時のチェック処理を追加する。     山城 2004/11/25 */
/* ･NO005：テスト項目の設定を３学期は、期末のみ設定 山城 2004/11/29 */
/* ･NO006：テスト項目コンボからラジオに変更         山城 2005/01/27 */
/* ･NO007：6の修正を戻し項目を変更                  山城 2005/01/31 */
/* ･NO008：クラスリストToリスト作成                 山城 2005/05/13 */
/********************************************************************/

class knjd060Form1
{
    function main(&$model){

$objForm = new form;
//フォーム作成
$arg["start"]   = $objForm->get_start("knjd060Form1", "POST", "knjd060index.php", "", "knjd060Form1");

$opt=array();


//年度テキストボックスを作成する

$arg["data"]["YEAR"] = $model->control["年度"];


//学期コンボボックスを作成する
if (is_numeric($model->control["学期数"])){
    //年度,学期コンボの設定
    for ( $i = 0; $i < (int) $model->control["学期数"]; $i++ ){
        $opt[]= array("label" => $model->control["学期名"][$i+1], 
                      "value" => sprintf("%d", $i+1)
                     );            
    }
    if (!$model->field["SEMESTER"]) $model->field["SEMESTER"] = $model->control["学期"];
    $seme = $model->field["SEMESTER"];
}
//学年末の場合、$semeを今学期にする。
if( isset($seme) ){
//    if( $seme == 9 ){
//		$seme = $model->control["学期"];	/* NO001 */
//    }
/* NO004↓ */
	switch ($seme) {
		case 9:
			$seme  = $model->control["学期"];	/* NO001 */
			$sseme = $model->control["学期開始日付"][9];
			$eseme = $model->control["学期終了日付"][9];
			$semeflg = $model->control["学期"];
			break;
		case 1:
			$sseme = $model->control["学期開始日付"][1];
			$eseme = $model->control["学期終了日付"][1];
			$semeflg = 1;
			break;
		case 2:
			$sseme = $model->control["学期開始日付"][2];
			$eseme = $model->control["学期終了日付"][2];
			$semeflg = 2;
			break;
		case 3:
			$sseme = $model->control["学期開始日付"][3];
			$eseme = $model->control["学期終了日付"][3];
			$semeflg = 3;
			break;
        default:
            $this->warning = "学期データが不正です。";
            return false;
	}
/* NO004↑ */


}
$opt[]= array("label" => $model->control["学期名"][9], 
               "value" => sprintf("%d", 9)
			  );

$objForm->ae( array("type"       => "select",
                    "name"       => "SEMESTER",
                    "size"       => "1",
                    "value"      => isset($model->field["SEMESTER"])?$model->field["SEMESTER"]:$model->control["学期"],
					"extrahtml"  => "onchange=\"return btn_submit('gakki');\"",    
                    "options"    => $opt));

$arg["data"]["SEMESTER"] = $objForm->ge("SEMESTER");

//学年コンボボックスを作成する NO008
$opt_schooldiv = "学年";

$db = Query::dbCheckOut();
$opt_grade=array();
$query = knjd060Query::getSelectGrade($model);
$result = $db->query($query);
$grade_flg = true;
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    $grade_show= sprintf("%d",$row["GRADE"]);
	$opt_grade[] = array('label' => $grade_show.$opt_schooldiv,
						 'value' => $row["GRADE"]);
	if( $model->field["GRADE"]==$row["GRADE"] ) $grade_flg = false;
}
if( $grade_flg ) $model->field["GRADE"] = $opt_grade[0]["value"];
$result->free();
Query::dbCheckIn($db);

$objForm->ae( array("type"       => "select",
                    "name"       => "GRADE",
                    "size"       => "1",
                    "value"      => $model->field["GRADE"],
					"extrahtml"  => "onChange=\"return btn_submit('knjd060');\"",
                    "options"    => $opt_grade));

$arg["data"]["GRADE"] = $objForm->ge("GRADE");



//テスト種別リスト  04/09/14  yamauchi

if($model->field["SEMESTER"] != 9 )
{
	if ($model->field["SEMESTER"] != 3 )
	{
/* NO005 ↓ */
	   	$opt_kind[]= array('label' => '0101　中間成績一覧',	/* NO007 */
	                       'value' => '0101');
   		$opt_kind[]= array('label' => '0201　期末成績一覧',	/* NO007 */
        	               'value' => '0201');
   		$opt_kind[]= array('label' => '0000　評価成績一覧',	/* NO007 */
        	               'value' => '0');
/* NO005 ↑ */
	}else {
   		$opt_kind[]= array('label' => '0201　期末成績一覧',	/* NO007 */
        	               'value' => '0201');
   		$opt_kind[]= array('label' => '0000　評価成績一覧',	/* NO007 */
        	               'value' => '0');
	}
}else {
	$opt_kind[]= array('label' => '0000　評価成績一覧',	/* NO007 */
       	               'value' => '0');
}
if ($model->field["TESTKINDCD"]=="") $model->field["TESTKINDCD"] = $opt_kind[0]["value"];

$objForm->ae( array("type"       => "select",
                    "name"       => "TESTKINDCD",
                    "size"       => "1",
                    "value"      => $model->field["TESTKINDCD"],
					"extrahtml"  => "STYLE=\"WIDTH:130\" ",
                    "options"    => $opt_kind));

$arg["data"]["TESTKINDCD"] = $objForm->ge("TESTKINDCD");

//NO006↓NO007以下NO006をコメント化
//テスト種別ラジオを作成
//	$opt_test[] = array();
//	$opt_test[] = '0000';
//	$opt_test[] = '0101';
//	$opt_test[] = '0201';

//	if (!isset($model->field["TESTKINDCD"]) || ($model->cmd == "gakki")){
//		if ($model->field["SEMESTER"] == 9){
//			$model->field["TESTKINDCD"] = '0000';
//		}else if ($model->field["SEMESTER"] == 3){
//			$model->field["TESTKINDCD"] = '0201';
//		}else {
//			$model->field["TESTKINDCD"] = '0101';
//		}
//	}

//	$objForm->ae( array("type"       => "radio",
//    	                "name"       => "TESTKINDCD",
//						"value"      => $model->field["TESTKINDCD"],
//						"multiple"   => $opt_test));

//	if ($model->field["SEMESTER"] == 9){
//		$arg["datara"]["TESTKINDCD4"] = $objForm->ge("TESTKINDCD",'0000');
//	}else if ($model->field["SEMESTER"] == 3){
//		$arg["datara"]["TESTKINDCD1"] = $objForm->ge("TESTKINDCD",'0000');
//		$arg["datara"]["TESTKINDCD3"] = $objForm->ge("TESTKINDCD",'0201');
//	}else {
//		$arg["datara"]["TESTKINDCD1"] = $objForm->ge("TESTKINDCD",'0000');
//		$arg["datara"]["TESTKINDCD2"] = $objForm->ge("TESTKINDCD",'0101');
//		$arg["datara"]["TESTKINDCD3"] = $objForm->ge("TESTKINDCD",'0201');
//	}
//NO006↑


//クラス一覧リスト作成する NO008↓
$db = Query::dbCheckOut();
//---2005.06.01---↓---
$semester = ($model->field["SEMESTER"]=="9") ? CTRL_SEMESTER : $model->field["SEMESTER"];
$query = common::getHrClassAuth(CTRL_YEAR,$semester,AUTHORITY,STAFFCD);
//$query = knjd060Query::getAuth($model);
//---2005.06.01---↑---
$result = $db->query($query);
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    if (substr($row["VALUE"],0,2) != $model->field["GRADE"]) continue;//---2005.06.01
    $row1[]= array('label' => $row["LABEL"],
                    'value' => $row["VALUE"]);
}
$result->free();
Query::dbCheckIn($db);

$objForm->ae( array("type"       => "select",
                    "name"       => "CLASS_NAME",
					"extrahtml"  => "multiple style=\"width=230px\" width=\"230px\" ondblclick=\"move1('left')\"",
                    "size"       => "20",
                    "options"    => isset($row1)?$row1:array()));

$arg["data"]["CLASS_NAME"] = $objForm->ge("CLASS_NAME");


//出力対象クラスリストを作成する///////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"       => "select",
                    "name"       => "CLASS_SELECTED",
					"extrahtml"  => "multiple style=\"width=230px\" width=\"230px\" ondblclick=\"move1('right')\"",
                    "size"       => "20",
                    "options"    => array()));

$arg["data"]["CLASS_SELECTED"] = $objForm->ge("CLASS_SELECTED");


//対象選択ボタンを作成する（全部）/////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" => "button",
                    "name"        => "btn_rights",
                    "value"       => ">>",
                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('right');\"" ) );

$arg["button"]["btn_rights"] = $objForm->ge("btn_rights");


//対象取消ボタンを作成する（全部）//////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" => "button",
                    "name"        => "btn_lefts",
                    "value"       => "<<",
                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('left');\"" ) );

$arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");


//対象選択ボタンを作成する（一部）//////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" => "button",
                    "name"        => "btn_right1",
                    "value"       => "＞",
                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('right');\"" ) );

$arg["button"]["btn_right1"] = $objForm->ge("btn_right1");


//対象取消ボタンを作成する（一部）//////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" => "button",
                    "name"        => "btn_left1",
                    "value"       => "＜",
                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('left');\"" ) );

$arg["button"]["btn_left1"] = $objForm->ge("btn_left1");
/* NO008 ↑ */

//チェックボックスを作成する
if($model->field["OUTPUT4"]=="1")
{
	$check = "checked";
} else {
	$check = "";
}
$objForm->ae( array("type"       => "checkbox",
                    "name"       => "OUTPUT4",
					"extrahtml"  => "".$check,    
					"value"      => isset($model->field["OUTPUT4"])?$model->field["OUTPUT4"]:1));

$arg["data"]["OUTPUT4"] = $objForm->ge("OUTPUT4");
//欠課処理チェックボックスを作成する  NO003↓
if($model->field["OUTPUT3"]=="1" || $model->cmd == "")
{
	$check2 = "checked";
} else {
	$check2 = "";
}
$objForm->ae( array("type"       => "checkbox",
               		"name"       => "OUTPUT3",
                    "extrahtml"  => "".$check2,
					"value"      => isset($model->field["OUTPUT3"])?$model->field["OUTPUT3"]:1));

$arg["data"]["OUTPUT3"] = $objForm->ge("OUTPUT3");
//////////////////////////////////////NO003↑

$value = isset($model->field["DATE"])?$model->field["DATE"]:$model->control["学籍処理日"];
//カレンダーコントロール
$arg["el"]["DATE"] = View::popUpCalendar($objForm,"DATE",$value);

//印刷ボタンを作成する
$objForm->ae( array("type" => "button",
                    "name"        => "btn_print",
                    "value"       => "プレビュー／印刷",
                    "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

$arg["button"]["btn_print"] = $objForm->ge("btn_print");

//終了ボタンを作成する
$objForm->ae( array("type" => "button",
                    "name"        => "btn_end",
                    "value"       => "終 了",
                    "extrahtml"   => "onclick=\"closeWin();\"" ) );

$arg["button"]["btn_end"] = $objForm->ge("btn_end");

//hiddenを作成する
$objForm->ae( array("type"      => "hidden",
                    "name"      => "DBNAME",
                    "value"      => DB_DATABASE
                    ) );


$objForm->ae( array("type"      => "hidden",
                    "name"      => "PRGID",
                    "value"     => "KNJD060"
                    ) );


$objForm->ae( array("type"      => "hidden",
                    "name"      => "cmd"
                    ) );

/* NO004↓ */
//学期
$objForm->ae( array("type"      => "hidden",
                    "name"      => "SEME_DATE",
                    "value"     => $seme
                    ) );

//学期開始日付
$objForm->ae( array("type"      => "hidden",
                    "name"      => "SEME_SDATE",
                    "value"     => $sseme
                    ) );

//学期終了日付
$objForm->ae( array("type"      => "hidden",
                    "name"      => "SEME_EDATE",
                    "value"     => $eseme
                    ) );

//学期終了日付
$objForm->ae( array("type"      => "hidden",
                    "name"      => "SEME_FLG",
                    "value"     => $semeflg
                    ) );

/* NO004↑ */

//年度
$objForm->ae( array("type"      => "hidden",
                    "name"      => "YEAR",
                    "value"      => $model->control["年度"]
                    ) );

knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);

$arg["finish"]  = $objForm->get_finish();
//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
View::toHTML($model, "knjd060Form1.html", $arg); 
}
}
?>
