<?php

require_once('for_php7.php');

class knja062aForm2
{
    public function main(&$model)
    {
        $objForm        = new form();

        if (!isset($model->warning)) {
            $Row = knja062aQuery::getRow($model, $model->term, $model->grade, $model->hr_class);
        } else {
            $Row =& $model->fields;
        }

        //学年
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["GRADE"] = knjCreateTextBox($objForm, $Row["GRADE"], "GRADE", 2, 2, $extra);

        //組
        $extra = "";
        $arg["data"]["HR_CLASS"] = knjCreateTextBox($objForm, $Row["HR_CLASS"], "HR_CLASS", 3, 3, $extra);

        //クラス形態
        $opt = array(1, 2);
        $Row["RECORD_DIV"] = ($Row["RECORD_DIV"] == "") ? "1" : $Row["RECORD_DIV"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"RECORD_DIV{$val}\" ");
        }
        $radioArray = knjCreateRadio($objForm, "RECORD_DIV", $Row["RECORD_DIV"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //年組名称
        $extra = "";
        $arg["data"]["HR_NAME"] = knjCreateTextBox($objForm, $Row["HR_NAME"], "HR_NAME", 11, 10, $extra);

        //年組略称
        $extra = "";
        $arg["data"]["HR_NAMEABBV"] = knjCreateTextBox($objForm, $Row["HR_NAMEABBV"], "HR_NAMEABBV", 6, 5, $extra);

        //組略称1
        $extra = "";
        $arg["data"]["HR_CLASS_NAME1"] = knjCreateTextBox($objForm, $Row["HR_CLASS_NAME1"], "HR_CLASS_NAME1", 20, 10, $extra);

        //組略称2
        $extra = "";
        $arg["data"]["HR_CLASS_NAME2"] = knjCreateTextBox($objForm, $Row["HR_CLASS_NAME2"], "HR_CLASS_NAME2", 20, 10, $extra);

        //担任１
        $arg["data"]["TR_CD1"] = knja062aquery::getStaff($model->term_year, $Row["TR_CD1"]);
        //担任2
        $arg["data"]["TR_CD2"] = knja062aquery::getStaff($model->term_year, $Row["TR_CD2"]);
        //担任3
        $arg["data"]["TR_CD3"] = knja062aquery::getStaff($model->term_year, $Row["TR_CD3"]);
        //副担任１
        $arg["data"]["SUBTR_CD1"] = knja062aquery::getStaff($model->term_year, $Row["SUBTR_CD1"]);
        //副担任２
        $arg["data"]["SUBTR_CD2"] = knja062aquery::getStaff($model->term_year, $Row["SUBTR_CD2"]);
        //副担任３
        $arg["data"]["SUBTR_CD3"] = knja062aquery::getStaff($model->term_year, $Row["SUBTR_CD3"]);

        //学期授業週数
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["CLASSWEEKS"] = knjCreateTextBox($objForm, $Row["CLASSWEEKS"], "CLASSWEEKS", 3, 2, $extra);

        //学期授業日数
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["CLASSDAYS"] = knjCreateTextBox($objForm, $Row["CLASSDAYS"], "CLASSDAYS", 4, 3, $extra);

        //表示・非表示
        $db = Query::dbCheckOut();
        $securityCnt = $db->getOne(knja062aQuery::getSecurityHigh());
        if ($model->getPrgId || !$model->Properties["useXLS"] || $securityCnt == 0) {
            //CSVファイルアップロードコントロール
            makeCsv($objForm, $arg, $model, $db);
            $arg["show_csv"] = "ON";
        } else {
            unset($arg["show_csv"]);
        }
        Query::dbCheckIn($db);


        /**********/
        /* ボタン */
        /**********/
        //担任追加ボタン
        $extra  = !$Row["HR_CLASS"] ? " disabled " : "";
        $extra .= " onClick=\" wopen('".REQUESTROOT."/A/KNJA062AS1/knja062as1index.php?";
        $extra .= "cmd=";
        $extra .= "&YEAR=".$model->term_year;
        $extra .= "&SEMESTER=".$model->term_semester;
        $extra .= "&GRADE=".$Row["GRADE"];
        $extra .= "&HR_CLASS=".$Row["HR_CLASS"];
        $extra .= "','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["button"]["btn_staff"] = knjCreateBtn($objForm, "btn_staff", "担任追加", $extra);

        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //クリアボタン
        $extra = "onclick=\"return btn_submit('reset')\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);
        knjCreateHidden($objForm, "TR_CD1", $Row["TR_CD1"]);
        knjCreateHidden($objForm, "TR_CD2", $Row["TR_CD2"]);
        knjCreateHidden($objForm, "TR_CD3", $Row["TR_CD3"]);
        knjCreateHidden($objForm, "SUBTR_CD1", $Row["SUBTR_CD1"]);
        knjCreateHidden($objForm, "SUBTR_CD2", $Row["SUBTR_CD2"]);
        knjCreateHidden($objForm, "SUBTR_CD3", $Row["SUBTR_CD3"]);
        //hidden(エクセル出力用)
        knjCreateHidden($objForm, "PARAM_YEAR_SEMESTER", $model->term);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJA062A");
        knjCreateHidden($objForm, "TEMPLATE_PATH");

        $arg["start"]   = $objForm->get_start("edit", "POST", "knja062aindex.php", "", "edit");
        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "window.open('knja062aindex.php?cmd=list&ed=1','left_frame');";
        }

        View::toHTML($model, "knja062aForm2.html", $arg);
    }
}

//ＣＳＶ作成
function makeCsv(&$objForm, &$arg, $model, $db)
{
    //出力取込種別ラジオボタン 1:取込 2:書出 3:エラー書出 4:見本
    $opt_shubetsu = array(1, 2, 3, 4);
    $model->field["OUTPUT"] = ($model->field["OUTPUT"]) ? $model->field["OUTPUT"] : "1";
    $click = " onclick=\"return changeRadio(this);\"";
    $extra = array("id=\"OUTPUT1\"".$click, "id=\"OUTPUT2\"".$click, "id=\"OUTPUT3\"".$click, "id=\"OUTPUT4\"".$click);
    $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, get_count($opt_shubetsu));
    foreach ($radioArray as $key => $val) {
        $arg["data"][$key] = $val;
    }

    //ファイルからの取り込み
    $extra = ($model->field["OUTPUT"] == "1") ? "" : "disabled";
    $arg["FILE"] = knjCreateFile($objForm, "FILE", $extra, 1024000);

    //ヘッダ有チェックボックス
    $check_header = "checked id=\"HEADER\"";
    $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $check_header, "");

    //実行ボタン
    if ($model->Properties["useXLS"]) {
        $model->schoolCd = $db->getOne(knja062aQuery::getSchoolCd());
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "', '" . $model->schoolCd . "', '" . $model->Properties["xlsVer"] . "');\"";
        $arg["data"]["CSV_XLS_NAME"] = "エクセル出力<BR>／ＣＳＶ取込";
    } else {
        $extra = "onclick=\"return btn_submit('exec');\"";
        $arg["data"]["CSV_XLS_NAME"] = "ＣＳＶ出力<BR>／ＣＳＶ取込";
    }
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
}
