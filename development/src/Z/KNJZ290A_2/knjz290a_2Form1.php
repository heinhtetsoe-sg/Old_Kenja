<?php

require_once('for_php7.php');

class knjz290a_2Form1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();
        
        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjz290a_2index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //学校名取得
        $model->getSchoolName = $db->getOne(knjz290a_2Query::getSchoolName());

        //年度コンボ
        $query = knjz290a_2Query::getStaffYear();
        $extra = "onchange=\"return btn_submit('chg_year');\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->year, $extra, 1, $model);

        //年度詳細データコピー
        $disflg = $model->year === "ALL" ? " disabled" : "";
        $btnSize = " style=\"font-size:10px;\"";
        $hyouji = "職名等前年度からコピー";
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", $hyouji, $extra.$btnSize.$disflg);

        //職員リスト
        $query = knjz290a_2Query::getList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            if ($row["CHARGECLASSCD"] != "") {
                if ($row["CHARGECLASSCD"] == 0) {
                    $row["CHARGECLASSCD"] = "0  無し";
                } elseif ($row["CHARGECLASSCD"] == 1) {
                    $row["CHARGECLASSCD"] = "1  有り";
                }
            }
            //各名称を取得
            if ($row["POSITIONCD1"] === '0200') {
                $row["POSITIONCD1_MANAGER_NAME"] = $db->getOne(knjz290a_2Query::getGrade($model, $row["POSITIONCD1_MANAGER"]));
            } elseif ($row["POSITIONCD1"] === '1050') {
                if ($model->getSchoolName != 'sapporo') {
                    $row["POSITIONCD1_MANAGER_NAME"] = $db->getOne(knjz290a_2Query::getClass($model, $row["POSITIONCD1_MANAGER"]));
                } else {
                    $row["POSITIONCD1_MANAGER_NAME"] = $db->getOne(knjz290a_2Query::getIbClass($model, $row["POSITIONCD1_MANAGER"]));
                }
            }
            if ($row["POSITIONCD2"] === '0200') {
                $row["POSITIONCD2_MANAGER_NAME"] = $db->getOne(knjz290a_2Query::getGrade($model, $row["POSITIONCD2_MANAGER"]));
            } elseif ($row["POSITIONCD2"] === '1050') {
                if ($model->getSchoolName != 'sapporo') {
                    $row["POSITIONCD2_MANAGER_NAME"] = $db->getOne(knjz290a_2Query::getClass($model, $row["POSITIONCD2_MANAGER"]));
                } else {
                    $row["POSITIONCD2_MANAGER_NAME"] = $db->getOne(knjz290a_2Query::getIbClass($model, $row["POSITIONCD2_MANAGER"]));
                }
            }
            if ($row["POSITIONCD3"] === '0200') {
                $row["POSITIONCD3_MANAGER_NAME"] = $db->getOne(knjz290a_2Query::getGrade($model, $row["POSITIONCD3_MANAGER"]));
            } elseif ($row["POSITIONCD3"] === '1050') {
                if ($model->getSchoolName != 'sapporo') {
                    $row["POSITIONCD3_MANAGER_NAME"] = $db->getOne(knjz290a_2Query::getClass($model, $row["POSITIONCD3_MANAGER"]));
                } else {
                    $row["POSITIONCD3_MANAGER_NAME"] = $db->getOne(knjz290a_2Query::getIbClass($model, $row["POSITIONCD3_MANAGER"]));
                }
            }

            if ($row["STAFFCD"] == $model->staffcd && $model->cmd != "chg_year") {
                $row["STAFFNAME"] = ($row["STAFFNAME"]) ? $row["STAFFNAME"] : "　";
                $row["STAFFNAME"] = "<a name=\"target\">{$row["STAFFNAME"]}</a><script>location.href='#target';</script>";
            }
            $row["STAFFBIRTHDAY"] = str_replace("-", "/", $row["STAFFBIRTHDAY"]);
            $arg["data"][] = $row;
        }
        $result->free();

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "list") {
            $arg["reload"] = "parent.right_frame.location.href='knjz290a_2index.php?cmd=new';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz290a_2Form1.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $model)
{
    $opt = array();
    if ($name == "YEAR") {
        $opt[] = array('label' => ' －全て－ ', 'value' => "ALL");
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($name == "YEAR" && $model->sendPrgid == "") {
            break;
        }

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    if ($name == "YEAR") {
        $default = ($model->sendPrgid == "") ? "ALL" : CTRL_YEAR;
        $value = ($value && $value_flg) ? $value : (($value == "ALL") ? $opt[0]["value"] : $default);
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
