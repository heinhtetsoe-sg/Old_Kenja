<?php

require_once('for_php7.php');

class knjz234aForm1
{
    public function main(&$model)
    {
        $objForm      = new form();
        $arg["start"] = $objForm->get_start("list", "POST", "knjz234aindex.php", "", "list");

        //権限チェック
        authCheck($arg);
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR."年度";

        //対象学期
        $query = knjz234aQuery::getSemester();
        $extra = " onchange=\"btn_submit('list');\"";
        $model->target_seme = ($model->target_seme == "") ? CTRL_SEMESTER : $model->target_seme;
        makeCmb($objForm, $arg, $db, $query, "TARGET_SEME", $model->target_seme, $extra, 1, "");

        //参照学期
        $query = knjz234aQuery::getSemester2($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "REFER_SEME", $model->refer_seme, $extra, 1, "");

        //コピーボタン
        $disabled = ($model->refer_seme == "") ? " disabled" :"";
        $extra = "onclick=\"return btn_submit('copy');\"".$disabled;
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "左の学期のデータをコピー", $extra);

        //学籍在籍データ件数
        $regd_cnt = $db->getOne(knjz234AQuery::getRegdDatCnt($model));
        $flg = ($regd_cnt > 0) ? "" : 1;

        //コースコンボ作成
        $opt = array();
        if ($model->cmd == "list") {
            $opt[] = array("label" => "", "value" => "");
        }
        $value_flg = false;
        $result = $db->query(knjz234AQuery::getCouseName($model, $flg));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $course_majaorne = $row["COURSENAME"].$row["MAJORNAME"];
            $name = ($row["COURSENAME"].$row["MAJORNAME"].str_repeat("&nbsp;", $space_count));

            $opt[] = array("label" => $row["GRADE_NAME1"]."&nbsp;".
                                        "(".$row["COURSECD"].$row["MAJORCD"].")&nbsp;".
                                        $name."&nbsp;".
                                        "(".$row["COURSECODE"].")&nbsp;"
                                        .$row["COURSECODENAME"],
                           "value" => $row["COURSECODE"]." ".$row["COURSECD"]." ".$row["MAJORCD"]." ".$row["GRADE"]);
            if ($model->coursename == $row["COURSECODE"]." ".$row["COURSECD"]." ".$row["MAJORCD"]." ".$row["GRADE"]) {
                $value_flg = true;
            }
        }
        //コース名
        $model->coursename = ($model->coursename && $value_flg) ? $model->coursename : $opt[0]["value"];
        $extra = " onchange=\"btn_submit('course');\"";
        $arg["COURSENAME"] = knjCreateCombo($objForm, "COURSENAME", $model->coursename, $opt, $extra, 1);

        //テストコンボ
        //テスト種別コードの設定時は表示しない
        if ($model->Properties["useKoteiTestCd"] !== '1') {
            $arg["useRegistCreditSubclass"] = 1;
            $opt = array();
            if ($model->Properties["use_CHAIR_GROUP_SDIV_DAT"] === '1') {
                $opt[] = array('label' => '000000:全考査', 'value' => '000000');
            }
            $query = knjz234aQuery::getTestItem($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
            }
            $extra = "onChange=\"btn_submit('course')\";";
            $arg["TEST_CD"] = knjCreateCombo($objForm, "TEST_CD", $model->field["TEST_CD"], $opt, $extra, 1);
        }

        //講座グループリスト
        makeChairGroupList($arg, $db, $model);

        //hidden
        $objForm->ae(createHiddenAe("cmd"));

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if ($model->cmd=='course') {
            $arg["reload"] = "window.open('knjz234aindex.php?cmd=init', 'right_frame')";
        }

        View::toHTML($model, "knjz234aForm1.html", $arg);
    }
}

//権限チェック
function authCheck(&$arg)
{
    if (AUTHORITY != DEF_UPDATABLE) {
        $arg["jscript"] = "OnAuthError();";
    }
}

//講座グループリスト
function makeChairGroupList(&$arg, $db, $model)
{
    $result = $db->query(knjz234aQuery::getChairMst($model));
    $aryPutSubclass = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_walk($row, "htmlspecialchars_array");
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $row["SUBCLASS_SET"] = $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"];
        } else {
            $row["SUBCLASS_SET"] = $row["SUBCLASSCD"];
        }
        $row["COURSECD_SET"] = $row["COURSECODE"]." ".$row["COURSECD"]." ".$row["MAJORCD"]." ".$row["GRADE"];
        //テスト種別コードの設定
        if ($model->Properties["useKoteiTestCd"] === '1') {
            //テスト種別なし
            if (($row["TEST_CD"] == $model->field['TEST_CD'] && substr($row["CHAIR_GROUP_CD"], 0, 3) === '000') || ($row["CHAIR_GROUP_CD"] == '' && $row["TEST_CD"] == "")) {
                //講座グループコード頭3桁が'000'ではない時、CHAIR_GROUP_MSTだけの通常作成されないデータは科目以外のデータを表示しない
                if ($row["TEST_CD"] == "" && $row["CHAIR_GROUP_CD"] != "" && substr($row["CHAIR_GROUP_CD"], 0, 3) !== '000') {
                    $row["CHAIR_GROUP_CD"] = "";
                    $row["CHAIR_GROUP_NAME"] = "";
                    $row["CHAIR_GROUP_ABBV"] = "";
                }
                //科目の重複箇所はまとめる
                if ($bifKey1 !== $row["SUBCLASS_SET"]) {
                    $cnt1 = $db->getOne(knjz234aQuery::getJyuhukuCnt($model, $row, $row["SUBCLASS_SET"], ""));
                    $row["ROWSPAN1"] = $cnt1 > 0 ? $cnt1 : 1;
                }
                $bifKey1 = $row["SUBCLASS_SET"];

                //グループコードの重複箇所はまとめる
                if ($bifKey2 !== $row["SUBCLASS_SET"].$row["CHAIR_GROUP_CD"]) {
                    $cnt2 = $db->getOne(knjz234aQuery::getJyuhukuCnt($model, $row, $row["SUBCLASS_SET"], "CHAIR_GROUP_CD"));
                    $row["ROWSPAN2"] = $cnt2 > 0 ? $cnt2 : 1;
                }

                //設定講座
                $row["CHAIRNAME"] = setListData($db, $model, $row["CHAIR_GROUP_CD"], $row["TEST_CD"], $row["CHAIRCD"]);

                $bifKey2 = $row["SUBCLASS_SET"].$row["CHAIR_GROUP_CD"];
                //表示用(頭3桁'000'は表示しない)
                $row["CHAIR_GROUP_CD_HYOUJI"] = substr($row["CHAIR_GROUP_CD"], 3, 3);

                $row['TEST_CD'] = $model->field['TEST_CD'];
                $arg["data"][] = $row;
            }
        } else {
            //テスト種別あり
            if ($row["TEST_CD"] == $model->field['TEST_CD'] || (($row["CHAIR_GROUP_CD"] == '' || $row["CHAIR_GROUP_CD"] == '9999') && $row["TEST_CD"] == '')) {
                if ($aryPutSubclass[$row["SUBCLASS_SET"]] && $row["CHAIR_GROUP_CD"] == '9999') {
                    continue;
                }
                //科目の重複箇所はまとめる
                if ($bifKey1 !== $row["SUBCLASS_SET"]) {
                    $cnt1 = $db->getOne(knjz234aQuery::getJyuhukuCnt($model, $row, $row["SUBCLASS_SET"], ""));
                    $row["ROWSPAN1"] = $cnt1 > 0 ? $cnt1 : 1;
                }
                $bifKey1 = $row["SUBCLASS_SET"];
                $aryPutSubclass[$row["SUBCLASS_SET"]] = true;

                //グループコードの重複箇所はまとめる
                if ($bifKey2 !== $row["SUBCLASS_SET"].$row["CHAIR_GROUP_CD"]) {
                    $cnt2 = $db->getOne(knjz234aQuery::getJyuhukuCnt($model, $row, $row["SUBCLASS_SET"], "CHAIR_GROUP_CD"));
                    $row["ROWSPAN2"] = $cnt2 > 0 ? $cnt2 : 1;
                }

                //設定講座
                $row["CHAIRNAME"] = setListData($db, $model, $row["CHAIR_GROUP_CD"], $row["TEST_CD"], $row["CHAIRCD"]);

                $bifKey2 = $row["SUBCLASS_SET"].$row["CHAIR_GROUP_CD"];

                //KNJZ234A_ShowOtherEntrySubclassが1の場合のみ9999が入る
                $row["CHAIR_GROUP_CD"] = $row["CHAIR_GROUP_CD"] == '9999' ? "" : $row["CHAIR_GROUP_CD"];
                //表示用
                $row["CHAIR_GROUP_CD_HYOUJI"] = $row["CHAIR_GROUP_CD"];

                $row['TEST_CD'] = $model->field['TEST_CD'];
                $arg["data"][] = $row;
            }
        }
    }
    $result->free();
}

//明細リストセット
function setListData($db, $model, $groupcd, $test_cd, $chaircd)
{
    $rtnData = "";
    $resultMdata = $db->query(knjz234aQuery::getGroupData($groupcd, $test_cd, $chaircd, $model));
    $rtnData .= "<td bgcolor=\"#ffffff\" nowrap>";
    while ($arow = $resultMdata->fetchRow(DB_FETCHMODE_ASSOC)) {
        $rtnData .= $arow["CHAIRCD"].' '.$arow["CHAIRNAME"]."<br> ";
    }
    $resultMdata->free();

    $rtnData .= "</tr>";

    return $rtnData;
}

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra)
{
    $objForm->ae(array("type"        => "button",
                        "name"        => $name,
                        "extrahtml"   => $extra,
                        "value"       => $value ));
    return $objForm->ge($name);
}

//Hidden作成ae
function createHiddenAe($name, $value = "")
{
    $opt_hidden = array();
    $opt_hidden = array("type"      => "hidden",
                        "name"      => $name,
                        "value"     => $value);
    return $opt_hidden;
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
