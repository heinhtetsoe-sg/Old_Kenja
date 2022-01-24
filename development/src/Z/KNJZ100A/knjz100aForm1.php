<?php

require_once('for_php7.php');

class knjz100aForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成//////////////////////////////////////////////////////////////////////////////////////////////
        $arg["start"]   = $objForm->get_start("knjz100aForm1", "POST", "knjz100aindex.php", "", "knjz100aForm1");

        $arg["data"]["YEAR"] = CTRL_YEAR;

        //出身学校・塾ラジオボタン 1:中学校選択 2:塾選択////////////////////////////////////////////////////////////
        $opt_div = array(1, 2);
        $model->field["FINSCHOOLDIV"] = ($model->field["FINSCHOOLDIV"] == "") ? "1" : $model->field["FINSCHOOLDIV"];
        $extra = array("id=\"FINSCHOOLDIV1\" onClick=\"return btn_submit('knjz100achangeDiv')\"", "id=\"FINSCHOOLDIV2\" onClick=\"return btn_submit('knjz100a')\"");
        $radioArray = knjCreateRadio($objForm, "FINSCHOOLDIV", $model->field["FINSCHOOLDIV"], $extra, $opt_div, get_count($opt_div));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        $db = Query::dbCheckOut();
        $model->isJyoto = $db->getOne(knjz100aQuery::getJyoto(CTRL_YEAR));

        if ($model->cmd == "" && $model->isJyoto) {
            $model->field["TO_PRINCIPAL"]  = "1";
            $model->field["PERSONNEL"]  = "1";
        }

        //学校長宛チェックボックス
        $extra = " id=\"TO_PRINCIPAL\"";
        $extra .= $model->field["TO_PRINCIPAL"] == "1" ? " checked" : "";
        $arg["data"]["TO_PRINCIPAL"] = knjCreateCheckBox($objForm, "TO_PRINCIPAL", 1, $extra);

        if ($model->isJyoto) {
            //担当者ありチェックボックス
            $extra = " id=\"PERSONNEL\" onChange=\"return btn_submit('knjz100a')\"";
            $extra .= $model->field["PERSONNEL"] == "1" ? " checked" : "";
            $arg["jyoto"]["PERSONNEL"] = knjCreateCheckBox($objForm, "PERSONNEL", 1, $extra);
        }

        if ($model->field["FINSCHOOLDIV"] == "1") {
            //校種コンボ
            $arg["finschooltype"] = "1";
            $extra = " onChange=\"return btn_submit('knjz100a')\"";
            $query = knjz100aQuery::getFinschoolTypeQuery($model);
            $useDefaultVal = $model->cmd == '' || $model->cmd == 'knjz100achangeDiv' ? 1 : 0;
            $addAll = 0 == $db->getOne(knjz100aQuery::getFinschoolTypeNullCount(CTRL_YEAR)) ? "" : "ALL";
            makeCmb($objForm, $arg, $db, $query, "SELECT_FINSCHOOL_TYPE", $model->field["SELECT_FINSCHOOL_TYPE"], $extra, 1, $useDefaultVal, $addAll);
        }

        //出身学校/塾一覧リスト作成する/////////////////////////////////////////////////////////////////////////////
        if ($model->field["FINSCHOOLDIV"] == "1") {
            if ($model->isJyoto) {
                $query = knjz100aQuery::selectFinSchoolQuery(CTRL_YEAR, $model->field["SELECT_FINSCHOOL_TYPE"], $model->field["PERSONNEL"]);
            } else {
                $query = knjz100aQuery::selectFinSchoolQuery(CTRL_YEAR, $model->field["SELECT_FINSCHOOL_TYPE"]);
            }
            $result = $db->query($query);
            $row1 = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row1[] = array("label" => $row["FINSCHOOLCD"]."  ".$row["FINSCHOOL_NAME"],
                                "value" => $row["FINSCHOOLCD"]);
            }
            $arg["data"]["NAME_LIST"] = '出身校';
            $result->free();
        } else {
            $result      = $db->query(knjz100aQuery::selectPriSchoolQuery(CTRL_YEAR));
            $row1 = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row1[]    = array("label" => $row["PRISCHOOLCD"]."  ".$row["PRISCHOOL_NAME"],
                                   "value" => $row["PRISCHOOLCD"]);
            }
            $arg["data"]["NAME_LIST"] = '塾';
            $result->free();
        }

        Query::dbCheckIn($db);

        $extra = "multiple style=\"width:220px;\" ondblclick=\"move1('left')\"";
        $arg["data"]["SCHOOL_NAME"] = knjCreateCombo($objForm, "SCHOOL_NAME", "", isset($row1)?$row1:array(), $extra, 20);
   

        //出力対象クラスリストを作成する///////////////////////////////////////////////////////////////////////////////////
        $extra = "multiple style=\"width:220px;\" ondblclick=\"move1('right')\"";
        $arg["data"]["SCHOOL_SELECTED"] = knjCreateCombo($objForm, "SCHOOL_SELECTED", "", array(), $extra, 20);


        //対象選択ボタンを作成する（全部）/////////////////////////////////////////////////////////////////////////////
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    

        //対象取消ボタンを作成する（全部）//////////////////////////////////////////////////////////////////////////////
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    

        //対象選択ボタンを作成する（一部）//////////////////////////////////////////////////////////////////////////////
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    

        //対象取消ボタンを作成する（一部）//////////////////////////////////////////////////////////////////////////////
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
    

        //開始位置（行）コンボボックスを作成する///////////////////////////////////////////////////////////////////////
        if ($model->isJyoto) {
            $row = array(array('label' => "１行",'value' => 1),
                         array('label' => "２行",'value' => 2),
                         array('label' => "３行",'value' => 3),
                         array('label' => "４行",'value' => 4),
                         array('label' => "５行",'value' => 5),
                         array('label' => "６行",'value' => 6)
            );
        } else {
            $row = array(array('label' => "１行",'value' => 1),
                         array('label' => "２行",'value' => 2),
                         array('label' => "３行",'value' => 3),
                         array('label' => "４行",'value' => 4),
                         array('label' => "５行",'value' => 5),
                         array('label' => "６行",'value' => 6),
                         array('label' => "７行",'value' => 7),
                         array('label' => "８行",'value' => 8)
            );
        }
        $arg["data"]["POROW"] = knjCreateCombo($objForm, "POROW", "", isset($row)?$row:array(), "", 1);


        //開始位置（列）コンボボックスを作成する////////////////////////////////////////////////////////////////////////
        if ($model->isJyoto) {
            $col = array(array('label' => "１列",'value' => 1),
                         array('label' => "２列",'value' => 2)
                        );
        } else {
            $col = array(array('label' => "１列",'value' => 1),
                         array('label' => "２列",'value' => 2),
                         array('label' => "３列",'value' => 3),
                        );
        }
        $arg["data"]["POCOL"] = knjCreateCombo($objForm, "POCOL", "", isset($col)?$col:array(), "", 1);


        //印刷ボタンを作成する///////////////////////////////////////////////////////////////////////////////////////////////
        $extra = "onclick=\"return newwin('".SERVLET_URL."');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    

        //終了ボタンを作成する//////////////////////////////////////////////////////////////////////////////////////////////
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
    

        //hiddenを作成する(必須)/////////////////////////////////////////////////////////////////////////////////////////////
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", $model->isJyoto ? "KNJZ100" : "KNJZ100A");
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "GAKKI", CTRL_SEMESTER);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "useAddrField2", $model->Properties["useAddrField2"]);
        knjCreateHidden($objForm, "notPrintFinschooltypeName", $model->Properties["notPrintFinschooltypeName"]);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();


        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz100aForm1.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $useDefaultVal, $all)
{
    $opt = array();
    $opt[] = array('label' => '', 'value' => '');
    $value_flg = false;
    $defvalue = '';
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
        if ($useDefaultVal && '1' == $row["IS_DEFAULT"]) {
            $defvalue = $row["VALUE"];
        }
    }
    $result->free();
    if ($all == "ALL") {
        $opt[] = array('label' => '-- 全て --', 'value' => '99');
        if ($value == "99") {
            $value_flg = true;
        }
    }
    if ($name == "SELECT_FINSCHOOL_TYPE") {
        $value = (($value === '0' || $value) && $value_flg) ? $value : ($defvalue ? $defvalue : $opt[0]["value"]);
    } else {
        $value = (($value === '0' || $value) && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
