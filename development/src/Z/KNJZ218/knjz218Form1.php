<?php

require_once('for_php7.php');

class knjz218Form1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("list", "POST", "knjz218index.php", "", "list");

        //権限チェック
        authCheck($arg);

        //DB接続
        $db = Query::dbCheckOut();

        //年度、学期表示
        $arg["YEAR"] = CTRL_YEAR."年度";

        //ボタン作成
        makeBtn($objForm, $arg);

        //学年
        $query = knjz218Query::getGrade();
        $extra = "onChange=\"return btn_submit('leftChange');\"";
        makeCmb($objForm, $arg, $db, $query, $model->leftGrade, "LEFT_GRADE", $extra);

        //類型グループリスト
        makeTypeGroupList($arg, $db, $model);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjz218Form1.html", $arg); 
    }
}

//権限チェック
function authCheck(&$arg)
{
    if (AUTHORITY != DEF_UPDATABLE) {
        $arg["jscript"] = "OnAuthError();";
    }
}

//類型グループリスト
function makeTypeGroupList(&$arg, $db, $model)
{
    $result = $db->query(knjz218Query::getTypeGroupList($model->leftGrade));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $mainData = "<tr> ";
        $link = View::alink("knjz218index.php",
                            $row["TYPE_GROUP_CD"]."　".$row["TYPE_GROUP_NAME"],
                            "target=right_frame",
                            array("cmd"             => "sel",
                                  "TYPE_GROUP_CD"   => $row["TYPE_GROUP_CD"],
                                  "GRADE"           => $row["GRADE"],
                                  "COURSECD"        => $row["COURSECD"],
                                  "MAJORCD"         => $row["MAJORCD"],
                                  "COURSECODE"      => $row["COURSECODE"]));
        //リンク
        $mainData .= "<td align=\"left\" bgcolor=\"#ffffff\" nowrap rowspan=".$row["CNT"].">".$link."</td>";
        //コース
        $setCourse = setListData($db, $model, $row["TYPE_GROUP_CD"], $row["GRADE"]);

        $arg["data"][]["MAINLIST"] = $mainData.$setCourse;
    }
    $result->free();
}

//明細リストセット
function setListData($db, $model, $type_group_cd, $grade)
{
    $cnt = 0;
    $rtnData = "";
    $resultMdata = $db->query(knjz218Query::getGroupData($type_group_cd, $grade));
    while ($arow = $resultMdata->fetchRow(DB_FETCHMODE_ASSOC)) {
        $rtnData .= ($cnt == 0) ? "" : "<tr> ";
        $rtnData .= "<td width=\"*%\" bgcolor=\"#ffffff\" nowrap>".$arow["COURSENAME"]."</td> ";
        $cnt++;
    }

    $resultMdata->free();

    $rtnData .= "</tr>";

    return $rtnData;
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra)
{
    $result = $db->query($query);
    $opt = array();

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
    }

    $value = ($value) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, 1);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //コピーボタン
    $extra = "onclick=\"return btn_submit('copy');\"";
    $arg["COPYBTN"] = knjCreateBtn($objForm, "COPYBTN", "前年度からコピー", $extra);
}

?>
