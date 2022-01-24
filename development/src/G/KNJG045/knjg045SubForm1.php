<?php

require_once('for_php7.php');

class knjg045SubForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1", "POST", "knjg045index.php", "", "subform1");

        //DB接続
        $db = Query::dbCheckOut();
        //更新後、親画面に反映
        $cmd = explode('-', $model->cmd);
        if ($cmd[1] == "A") {
            $sftDiv = array();
            $sftDiv["kesseki"]      = "1";
            $sftDiv["chikoku"]      = "2";
            $sftDiv["soutai"]       = "3";
            $sftDiv["shuchou"]      = "4";
            $sftDiv["hoketsu"]      = "5";
            $sftDiv["etc_hoketsu"]  = "6";

            $setStaffName = array();
            if ($sftDiv[$cmd[0]] == 5) {
                $query = knjg045Query::getStaffData($model, $model->setGradeHrClass);
            } else {
                $query = knjg045Query::getStaffData($model);
            }
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row["STAFF_DIV"] == $sftDiv[$cmd[0]]) {
                    if ($sftDiv[$cmd[0]] < 5) {
                        $setStaffName[] = $row["STAFFNAME_SHOW"];
                    } else {
                        $setStaffName[] = $row["STAFFNAME_SHOW"].'('.$row["COUNT"].')';
                    }
                }
            }
            knjCreateHidden($objForm, "setStaffName", implode(',', $setStaffName));
            //反映処理
            $arg["reload"] = "refStaffName('STAFFNAME_SHOW{$sftDiv[$cmd[0]]}');";
            $model->cmd = $cmd[0];
            //更新後は画面を閉じる
            $arg["close"] = "parent.closeit();";
        }

        //マスタ情報
        $arg["SHOKUIN_SET"] = '1';
        $setTitle = '職員・状況';

        $arg["TITLE"] = $setTitle;

        $rirekiCnt = makeList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjg045SubForm1.html", $arg);
    }
}

//リスト作成
function makeList(&$objForm, &$arg, $db, $model)
{

    if ($model->cmd === 'kesseki') {
        $arg["koumoku"] = '欠席者';
        $arg["subtitle"] = '(欠席者)';
        $arg["tujyou"] = '1';
        $setStaffDiv = '1';
    } elseif ($model->cmd === 'chikoku') {
        $arg["koumoku"] = '遅参者';
        $arg["subtitle"] = '(遅参者)';
        $arg["tujyou"] = '1';
        $setStaffDiv = '2';
    } elseif ($model->cmd === 'soutai') {
        $arg["koumoku"] = '早退者';
        $arg["subtitle"] = '(早退者)';
        $arg["tujyou"] = '1';
        $setStaffDiv = '3';
    } elseif ($model->cmd === 'shuchou') {
        $arg["koumoku"] = '出張者';
        $arg["subtitle"] = '(出張者)';
        $arg["tujyou"] = '1';
        $setStaffDiv = '4';
    } elseif ($model->cmd === 'hoketsu') {
        $arg["koumoku"] = '補欠授業';
        $arg["subtitle"] = '(補欠授業)';
        $arg["hoketsu"] = '1';
        $setStaffDiv = '5';
    } elseif ($model->cmd === 'etc_hoketsu') {
        $arg["koumoku"] = 'その他補欠';
        $arg["subtitle"] = '(その他補欠)';
        $arg["etc_hoketsu"] = '1';
        $setStaffDiv = '6';
    }

    //各データ取得
    $counter = 0;
    if (VARS::get("diary_date")) {
        $setDiaryDate = VARS::get("diary_date");
    } elseif ($model->setDiaryDate) {
        $setDiaryDate = $model->setDiaryDate;
    }
    if (VARS::get("grade_hr_class")) {
        $setGradeHrClass = VARS::get("grade_hr_class");
    } elseif ($model->setGradeHrClass) {
        $setGradeHrClass = $model->setGradeHrClass;
    }
    //各キーを保持
    knjCreateHidden($objForm, "setDiaryDate", $setDiaryDate);
    knjCreateHidden($objForm, "setStaffDiv", $setStaffDiv);
    knjCreateHidden($objForm, "setGradeHrClass", $setGradeHrClass);

    if ($setStaffDiv === '5') {
        $query = knjg045Query::getSelectSubQuery($model, $setDiaryDate, $setGradeHrClass);
    } else {
        $query = knjg045Query::getSelectSubQuery($model, $setDiaryDate);
    }
    $result = $db->query($query);
    while ( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //STAFFCDを配列で取得し、保持する
        $model->fields["STAFFCD"][$counter] = $row["STAFFCD"];
        knjCreateHidden($objForm, "STAFFCD"."_".$counter, $model->fields["STAFFCD"][$counter]);

        //選択（チェック）
        //決定者
        $value = (!isset($model->warning)) ? $row["CHECK"] : $model->fields["CHECK"][$counter];
        $extra  = ($value == "1") ? " checked" : "";
        $extra .= ($model->cmd === 'hoketsu' || $model->cmd === 'etc_hoketsu') ? " onclick=\"OptionUse(this);\"" : "";
        $setData["CHECK"] = knjCreateCheckBox($objForm, "CHECK"."_".$counter, "1", $extra);

        if ($model->cmd === 'hoketsu' || $model->cmd === 'etc_hoketsu') {
            //回数
            $value = (!isset($model->warning)) ? $row["COUNT"] : $model->fields["COUNT"][$counter];
            if ($value > 0) {
                $extra1 = "checked";
            } else {
                $extra1 = "";
            }
            $setData["COUNT_CHECK1"] = knjCreateCheckBox($objForm, "COUNT_CHECK1"."_".$counter, "1", $extra1);

            if ($value > 1) {
                $extra2 = "checked";
            } else {
                $extra2 = "";
            }
            $setData["COUNT_CHECK2"] = knjCreateCheckBox($objForm, "COUNT_CHECK2"."_".$counter, "1", $extra2);

            if ($value > 2) {
                $extra3 = "checked";
            } else {
                $extra3 = "";
            }
            $setData["COUNT_CHECK3"] = knjCreateCheckBox($objForm, "COUNT_CHECK3"."_".$counter, "1", $extra3);

            if ($model->cmd === 'etc_hoketsu') {
                if ($value > 3) {
                    $extra4 = "checked";
                } else {
                    $extra4 = "";
                }
                $setData["COUNT_CHECK4"] = knjCreateCheckBox($objForm, "COUNT_CHECK4"."_".$counter, "1", $extra4);

                if ($value > 4) {
                    $extra5 = "checked";
                } else {
                    $extra5 = "";
                }
                $setData["COUNT_CHECK5"] = knjCreateCheckBox($objForm, "COUNT_CHECK5"."_".$counter, "1", $extra5);
            }
            //回数を保持
            knjCreateHidden($objForm, "COUNT"."_".$counter, $model->fields["COUNT"][$counter]);
        }
        $setData["SET_STAFFCD"] = $row["STAFFCD"];
        $setData["STAFFNAME_SHOW"] = $row["STAFFNAME_SHOW"];
        $setData["backcolor1"] = ($row["CHECK"]) ? "1" : "2";  //#ccffff
        $setData["backcolor"] = ($setData["backcolor1"] == "1") ? "#ccffcc" : "#ffffff";  //#ccffff

        $arg["data"][] = $setData;
        $counter++;
    }
    $result->free();
    Query::dbCheckIn($db);
    //データ数を保持
    knjCreateHidden($objForm, "setcounter", $counter);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update_detail')\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //戻るボタン
    $extra = "onclick=\"return parent.closeit()\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "GET_CMD", $model->cmd);
}
