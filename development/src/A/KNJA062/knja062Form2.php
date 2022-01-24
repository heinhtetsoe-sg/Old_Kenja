<?php

require_once('for_php7.php');

class knja062Form2
{
    public function main(&$model)
    {
        $objForm        = new form();

        if (!isset($model->warning)) {
            $Row = knja062Query::getRowHdat($model->term, $model->ghr_cd);
        } else {
            $Row =& $model->fields;
        }

        //複式クラス
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["GHR_CD"] = knjCreateTextBox($objForm, $Row["GHR_CD"], "GHR_CD", 2, 2, $extra);

        //名称
        $extra = "";
        $arg["data"]["GHR_NAME"] = knjCreateTextBox($objForm, $Row["GHR_NAME"], "GHR_NAME", 20, 10, $extra);

        //略称
        $extra = "";
        $arg["data"]["GHR_NAMEABBV"] = knjCreateTextBox($objForm, $Row["GHR_NAMEABBV"], "GHR_NAMEABBV", 20, 10, $extra);

        //担任１
        $arg["data"]["TR_CD1"] = knja062query::getStaff($model->term_year, $Row["TR_CD1"]);
        //担任2
        $arg["data"]["TR_CD2"] = knja062query::getStaff($model->term_year, $Row["TR_CD2"]);
        //担任3
        $arg["data"]["TR_CD3"] = knja062query::getStaff($model->term_year, $Row["TR_CD3"]);
        //副担任１
        $arg["data"]["SUBTR_CD1"] = knja062query::getStaff($model->term_year, $Row["SUBTR_CD1"]);
        //副担任２
        $arg["data"]["SUBTR_CD2"] = knja062query::getStaff($model->term_year, $Row["SUBTR_CD2"]);
        //副担任３
        $arg["data"]["SUBTR_CD3"] = knja062query::getStaff($model->term_year, $Row["SUBTR_CD3"]);

        //表示・非表示
        $db = Query::dbCheckOut();
        $securityCnt = $db->getOne(knja062Query::getSecurityHigh());
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
        $extra  = !$Row["GHR_CD"] ? " disabled " : "";
        $extra .= " onClick=\" wopen('".REQUESTROOT."/A/KNJA062S1/knja062s1index.php?";
        $extra .= "cmd=";
        $extra .= "&YEAR=".$model->term_year;
        $extra .= "&SEMESTER=".$model->term_semester;
        $extra .= "&GHR_CD=".$Row["GHR_CD"];
        $extra .= "','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["button"]["btn_staff"] = knjCreateBtn($objForm, "btn_staff", "担任追加", $extra);

        //追加ボタン
        $objForm->ae(array("type"      => "button",
                            "name"      => "btn_add",
                            "value"     => "追 加",
                            "extrahtml" => "onclick=\"return btn_submit('add');\"" ));
        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //更新ボタン
        $objForm->ae(array("type" => "button",
                            "name"        => "btn_udpate",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ));
        $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");

        //削除ボタン
        $objForm->ae(array("type" => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ));
        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //クリアボタン
        $objForm->ae(array("type"           => "reset",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('reset')\"" ));
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタン
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ));
        $arg["button"]["btn_back"] = $objForm->ge("btn_back");

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
        knjCreateHidden($objForm, "PRGID", "KNJA062");
        knjCreateHidden($objForm, "TEMPLATE_PATH");

        $arg["start"]   = $objForm->get_start("edit", "POST", "knja062index.php", "", "edit");
        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "window.open('knja062index.php?cmd=list&ed=1','left_frame');";
        }

        View::toHTML($model, "knja062Form2.html", $arg);
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
        $model->schoolCd = $db->getOne(knja062Query::getSchoolCd());
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "', '" . $model->schoolCd . "', '" . $model->Properties["xlsVer"] . "');\"";
        $arg["data"]["CSV_XLS_NAME"] = "エクセル出力<BR>／ＣＳＶ取込";
    } else {
        $extra = "onclick=\"return btn_submit('exec');\"";
        $arg["data"]["CSV_XLS_NAME"] = "ＣＳＶ出力<BR>／ＣＳＶ取込";
    }
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
}
