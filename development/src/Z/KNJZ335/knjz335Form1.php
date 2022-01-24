<?php

require_once('for_php7.php');

class knjz335Form1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("list", "POST", "knjz335index.php", "", "list");

        //権限チェック
        authCheck($arg);
        //DB接続
        $db = Query::dbCheckOut();

        //科目リスト
        makeStaffList($arg, $db, $model);

        //対象年度
        $arg["year"] = CTRL_YEAR;

        //hidden
        $objForm->ae(createHiddenAe("cmd"));

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjz335Form1.html", $arg); 
    }
}
//権限チェック
function authCheck(&$arg)
{
    if (AUTHORITY != DEF_UPDATABLE) {
        $arg["jscript"] = "OnAuthError();";
    }
}

//教員リスト
function makeStaffList(&$arg, $db, $model)
{
    $result = $db->query(knjz335Query::getStaffMst($model, $model->classcd, "MAIN"));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $mainData = "<tr> ";

        //更新後この行にスクロールバーを移動させる
        if ($row["STAFFCD"] == $model->leftStaffCd) {
            $row["STAFFNAME"] = ($row["STAFFNAME"]) ? $row["STAFFNAME"] : "　";
            $row["STAFFNAME"] = "<a name=\"target\">{$row["STAFFNAME"]}</a><script>location.href='#target';</script>";
        }

        $link = View::alink("knjz335index.php",
                            $row["STAFFCD"],
                            "target=right_frame",
                            array("cmd"           => "sel",
                                  "LEFT_STAFFCD" => $row["STAFFCD"]));
        //職員
        $mainData .= "<td width=\"13%\" bgcolor=\"#ffffff\" nowrap align=\"center\" rowspan=".$row["CNT"].">".$link."</td> ";
        $mainData .= "<td width=\"30%\" bgcolor=\"#ffffff\" nowrap rowspan=".$row["CNT"].">".$row["STAFFNAME"]."</td> ";

        //掛持ちの学校
        $schoolCnt = 0;
        $changeSchool = "";
        $changeSchoolData = $db->query(knjz335Query::getChangeSchool($model, $row["STAFFCD"]));
        while ($arow = $changeSchoolData->fetchRow(DB_FETCHMODE_ASSOC)) {
            $changeSchool .= ($schoolCnt == 0) ? "" : "<tr> ";
            $changeSchool .= "<td width=\"20%\" bgcolor=\"#ffffff\" nowrap>".$arow["EDBOARD_SCHOOLCD"]."</td> ";
            $changeSchool .= "<td width=\"*%\" bgcolor=\"#ffffff\" nowrap>".$arow["EDBOARD_SCHOOLNAME"]."</td> </tr>";
            $schoolCnt++;
        }
        $changeSchoolData->free();

        if ($schoolCnt == 0) {
            $changeSchool .= ($schoolCnt == 0) ? "" : "<tr> ";
            $changeSchool .= "<td width=\"20%\" bgcolor=\"#ffffff\" nowrap>　</td> ";
            $changeSchool .= "<td width=\"*%\" bgcolor=\"#ffffff\" nowrap>　</td> </tr>";
        }

        $arg["data"][]["MAINLIST"] = $mainData.$changeSchool;
    }
    $result->free();
}

//コンボ作成
function createCombo(&$objForm, $name, $value, $options, $extra, $size)
{
    $objForm->ae( array("type"      => "select",
                        "name"      => $name,
                        "size"      => $size,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "options"   => $options));
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
