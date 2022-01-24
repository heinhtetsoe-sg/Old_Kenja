<?php

require_once('for_php7.php');

class knja233Form1
{
    function main(&$model){

    $objForm = new form;

    $arg = array();

    //フォーム作成
    $arg["start"]   = $objForm->get_start("main", "POST", "knja233index.php", "", "main");

    //DB接続
    $db = Query::dbCheckOut();

    //教育課程対応
    if ($model->Properties["useCurriculumcd"] == '1') {
        //表示用
        $arg["usecurriculumcd"] = 1;
    } else {
        $arg["Nocurriculumcd"] = 1;
    }

    //学習記録エクスプローラー
    if ($model->cmd != "toukei") {
        $arg["ONLOAD"] = "wopen('../../X/KNJXTOKE3/knjxtoke3index.php?DISP=CLASS&PROGRAMID=$model->programid&SEND_AUTH=$model->auth&SEND_PRGID=$model->getPrgId','KNJXTOKE3',0,0,900,550);";
    }

    if ($model->schoolName == 'MUSASHI') {
        $arg["is_musashi"] = 1;
        knjCreateHidden($objForm, "OUTPUT", 'musashi');
    } else {
        $arg["is_not_musashi"] = 1;
    }

    //読込ボタンを作成する
    $objForm->ae( array("type" => "button",
                        "name"        => "btn_toukei",
                        "value"       => "･･･",
                        "extrahtml"   => "onclick=\"wopen('../../X/KNJXTOKE3/knjxtoke3index.php?DISP=CLASS&PROGRAMID=$model->programid&SEND_AUTH=$model->auth&SEND_PRGID=$model->getPrgId','KNJXTOKE3',0,0,900,550);\"") );

    $arg["explore"] = $objForm->ge("btn_toukei");

    $cd =& $model->subclasscd;
    if (isset($cd)){
        //テーブルの行の色を塗るところと塗らないところを
        //区別するための配列を作る
        //MAX(APPENDDATE)が背景'白'
        //それ以外は背景'緑'
        //69行目あたりで使う
        $color_row = array();
        $query = knja233Query::SQLGet_Main_Color($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $color_row[$row["CHAIRCD"]] = $row["APPENDDATE"];
        }

        $query = knja233Query::SQLGet_Main($model);
        $i=0;
        //教科、科目、クラス取得
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $title = "[" . $row["CLASSCD"].'-'.$row["SCHOOL_KIND"]."　".htmlspecialchars($row["CLASSNAME"]) ."]";
                $subclasscd = $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"];
            } else {
                $title = "[" . $row["CLASSCD"]."　".htmlspecialchars($row["CLASSNAME"]) ."]";
                $subclasscd = $row["SUBCLASSCD"];
            }

            $checked = (is_array($model->checked_attend) && in_array($row["ATTENDCLASSCD"], $model->checked_attend))? true:false;
            if($checked==0) {
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $objForm->add_element(array("type"      => "checkbox",
                                                "name"     => "chk",
                                                "checked"  => $checked,
                                                "value"    => $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"].",".$row["ATTENDCLASSCD"].",".$row["GROUPCD"].",".$row["STAFFCD"].",".$row["CHARGEDIV"].",".$row["APPDATE"],
                                                "extrahtml"   => "multiple" ));
                } else {
                    $objForm->add_element(array("type"      => "checkbox",
                                                "name"     => "chk",
                                                "checked"  => $checked,
                                                "value"    => $row["SUBCLASSCD"].",".$row["ATTENDCLASSCD"].",".$row["GROUPCD"].",".$row["STAFFCD"].",".$row["CHARGEDIV"].",".$row["APPDATE"],
                                                "extrahtml"   => "multiple" ));
                }
                $row["CHECK"] = $objForm->ge("chk");

                $start = str_replace("-","/",$row["STARTDAY"]);
                $end = str_replace("-","/",$row["ENDDAY"]);
                //学籍処理範囲外の場合背景色を変える
                if ($color_row[$row["ATTENDCLASSCD"]] == $row["APPENDDATE"]) {
                    $row["BGCOLOR"] = "#ffffff";
                } else {
                    $row["BGCOLOR"] = "#ccffcc";
                }
                $row["TERM"] = $start ."～" .$end;

                if($row["CHARGEDIV"] == 1) {
                    $row["CHARGEDIV"] = ' ＊';
                }else {
                    $row["CHARGEDIV"] = ' ';
                }
                $row["APPDATE"] = str_replace("-","/",$row["APPDATE"]);
                $arg["data"][] = $row;
            }
            $i++;
            if($i==1) {
                $arg["data1"][] = $row;
            }
        }
    }

    $objForm->add_element(array("type"      => "checkbox",
                                "name"      => "chk_all",
                                "extrahtml"   => "onClick=\"return check_all();\"" ));

    $arg["CHECK_ALL"] = $objForm->ge("chk_all");

    if ($model->schoolName != 'MUSASHI') {
        //出力順序指定
        $opt[0]=1;
        $opt[1]=2;
        $opt[2]=3;
        $extra = 'onClick="hurigana();"';

        for ($i = 1; $i <= 3; $i++) {
            $label = " id=OUTPUT".$i;
            $objForm->ae( array("type"       => "radio",
                                "name"       => "OUTPUT",
                                "value"      => isset($model->field["OUTPUT"]) ? $model->field["OUTPUT"] : 1,
                                "extrahtml"  => $extra.$label,
                                "multiple"   => $opt));

            $arg["OUTPUT".$i] = $objForm->ge("OUTPUT", $i);
        }
    }
    //ふりがな出力
    $opt[0]=1;
    $opt[1]=2;
    if ($model->field["OUTPUT"] == '3') {
        $extra = 'disabled=true';
    } else {
        $extra = '';
    }

    for ($i = 1; $i <= 2; $i++) {
        $label = " id=HURIGANA_OUTPUT".$i;
        $objForm->ae( array("type"       => "radio",
                            "name"       => "HURIGANA_OUTPUT",
                            "value"      => isset($model->field["HURIGANA_OUTPUT"]) ? $model->field["HURIGANA_OUTPUT"] : 1,
                            "extrahtml"  => $extra.$label,
                            "multiple"   => $opt));

        $arg["HURIGANA_OUTPUT".$i] = $objForm->ge("HURIGANA_OUTPUT", $i);
    }

    //縦サイズ
    $opt = array();
    $opt[] = array("label" => "4ミリ", "value" => "4");
    $opt[] = array("label" => "5ミリ", "value" => "5");
    $default = $db->getOne(knja233Query::getDefaultSize("HEIGHT"));
    $value = ($default) ? $default : $opt[0]["value"];
    $arg["HEIGHT"] = knjCreateCombo($objForm, "HEIGHT", $value, $opt, "", 1);

    //縦サイズ
    $opt = array();
    for ($i = 25 ; $i <= 33 ; $i++) {
        $opt[] = array("label" => $i."ミリ", "value" => $i);
    }
    $default = $db->getOne(knja233Query::getDefaultSize("WIDTH"));
    $value = ($default) ? $default : $opt[0]["value"];
    $arg["WIDTH"] = knjCreateCombo($objForm, "WIDTH", $value, $opt, "", 1);

    //出力件数テキストボックス
    $objForm->ae( array("type"        => "text",
                        "name"        => "KENSUU",
                        "size"        => 3,
                        "maxlength"   => 2,
                        "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                        "value"       => isset($model->field["KENSUU"])?$model->field["KENSUU"]:1 ));
    $arg["KENSUU"] = $objForm->ge("KENSUU");

    //プレビューボタンを作成する
    $extra = "onclick=\"return opener_submit('" . SERVLET_URL . "');\"";
    $arg["btn_ok"] = knjCreateBtn($objForm, "btn_ok", "プレビュー／印刷", $extra);

    //ＣＳＶ出力ボタン
    $btnName = "ＣＳＶ出力";
    if ($model->Properties["useXLS"]) {
        $model->schoolCd = $db->getOne(knja233Query::getSchoolCd());
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "', '" . $model->schoolCd . "', '" . $model->Properties["xlsVer"] . "');\"";
        $btnName = "エクセル出力";
    } else {
        $extra = "onclick=\"return csv_submit('csv');\"";
    }
    //セキュリティーチェック
    $securityCnt = $db->getOne(knja233Query::getSecurityHigh());
    if ($model->getPrgId || !$model->Properties["useXLS"] || $securityCnt == 0) {
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", $btnName, $extra);
    }

    //終了ボタンを作成する
    $extra = "onClick=\"closeWin();\"";
    $arg["btn_can"] = knjCreateBtn($objForm, "btn_can", " 終 了 ", $extra);

    //タイトル
    $arg["TITLE"] = $title;

    //年度・学期（表示）
    if (($model->year != "") && ($model->semester != "")) {
        $arg["YEAR_SEMESTER"] = $model->year."年度&nbsp;" .$model->control["学期名"][$model->semester]."&nbsp;";
    }

    //hiddenを作成する
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID");
    knjCreateHidden($objForm, "YEAR", $model->year);
    knjCreateHidden($objForm, "SEMESTER", $model->semester);
    knjCreateHidden($objForm, "CLASSCD", $model->classcd);
    knjCreateHidden($objForm, "SUBCLASSCD", $subclasscd);
    knjCreateHidden($objForm, "TESTKINDCD", $model->testkindcd);
    knjCreateHidden($objForm, "TESTITEMCD", $model->testitemcd);
    knjCreateHidden($objForm, "ATTENDCLASSCD");
    knjCreateHidden($objForm, "GROUPCD");
    knjCreateHidden($objForm, "DISP");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "NAME_SHOW");
    knjCreateHidden($objForm, "CHARGEDIV");
    knjCreateHidden($objForm, "APPDATE");
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "TEMPLATE_PATH");
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

    //DB切断
    Query::dbCheckIn($db);

    $arg["finish"]  = $objForm->get_finish();

    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
    View::toHTML($model, "knja233Form1.html", $arg);
    }
}
?>
