<?php

require_once('for_php7.php');

class knjz234Form1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("list", "POST", "knjz234index.php", "", "list");

        //権限チェック
        authCheck($arg);
        //DB接続
        $db = Query::dbCheckOut();

        //年度、学期表示
        $arg["YEAR_SEM"] = CTRL_YEAR."年度".CTRL_SEMESTER."学期";

        //コピーボタン
        $extra = "onClick=\"return btn_submit('copy');\"";
        $arg["COPYBTN"] = createBtn($objForm, "COPYBTN", "前学期または、前年度からコピー", $extra);

        //講座グループリスト
        makeChairGroupList($arg, $db, $model);

        //hidden
        $objForm->ae(createHiddenAe("cmd"));

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjz234Form1.html", $arg); 
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
    $result = $db->query(knjz234Query::GetChairMst($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $mainData = "<tr> ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $link = View::alink("knjz234index.php",
                                $row["CHAIR_GROUP_CD"]."　".$row["CHAIR_GROUP_NAME"],
                                "target=right_frame",
                                array("cmd"             => "sel",
                                      "CHAIR_GROUP_CD"  => $row["CHAIR_GROUP_CD"],
                                      "TEST_CD"         => $row["TEST_CD"],
                                      "CLASSCD"         => $row["CLASSCD"],
                                      "SCHOOL_KIND"     => $row["SCHOOL_KIND"],
                                      "CURRICULUM_CD"   => $row["CURRICULUM_CD"],
                                      "SUBCLASSCD"      => $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"]));
        } else {
            $link = View::alink("knjz234index.php",
                                $row["CHAIR_GROUP_CD"]."　".$row["CHAIR_GROUP_NAME"],
                                "target=right_frame",
                                array("cmd" => "sel",
                                      "CHAIR_GROUP_CD" => $row["CHAIR_GROUP_CD"],
                                      "TEST_CD" => $row["TEST_CD"],
                                      "SUBCLASSCD" => $row["SUBCLASSCD"]));
        } 
        //リンク
        $mainData .= "<td align=\"left\" bgcolor=\"#ffffff\" nowrap >".$link."</td>";

        //科目名
        $subclallData = "<td align=\"left\" bgcolor=\"#ffffff\" nowrap >".$row["SUBCLASSNAME"]."</td>";

        //テスト名
        $testData = "<td align=\"left\" bgcolor=\"#ffffff\" nowrap >".$row["TESTITEMNAME"]."</td>";

        //設定講座
        $setChair = setListData($db, $model, $row["CHAIR_GROUP_CD"], $row["TEST_CD"]);

        $arg["data"][]["MAINLIST"] = $mainData.$subclallData.$testData.$setChair;
    }
    $result->free();
}

//明細リストセット
function setListData($db, $model, $groupcd, $test_cd)
{
    $rtnData = "";
    $resultMdata = $db->query(knjz234Query::getGroupData($groupcd, $test_cd));
    $rtnData .= "<td bgcolor=\"#ffffff\" nowrap>";
    while ($arow = $resultMdata->fetchRow(DB_FETCHMODE_ASSOC)) {
        $rtnData .= $arow["CHAIRNAME"]."<br> ";
    }
    $resultMdata->free();

    $rtnData .= "</tr>";

    return $rtnData;
}

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra)
{
    $objForm->ae( array("type"        => "button",
                        "name"        => $name,
                        "extrahtml"   => $extra,
                        "value"       => $value ) );
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

?>
