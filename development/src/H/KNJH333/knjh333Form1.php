<?php

require_once('for_php7.php');

class knjh333Form1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("list", "POST", "knjh333index.php", "", "list");

        //権限チェック
        authCheck($arg);
        //DB接続
        $db = Query::dbCheckOut();

        //参照年度コンボボックス
        $query = knjh333Query::getReferenceYear();
        $extra = "onChange=\"btn_submit('changeRyear')\";";
        makeCmb($objForm, $arg, $db, $query, $model->ryear, "RYEAR", $extra);

        //コピーボタン
        $extra = "onClick=\"return btn_submit('copy');\"";
        $arg["COPYBTN"] = createBtn($objForm, "COPYBTN", "左の年度データをコピー", $extra);

        //対象年度コンボボックス
        $query = knjh333Query::getObjectYear();
        $extra = "onChange=\"btn_submit('changeOyear')\";";
        makeCmb($objForm, $arg, $db, $query, $model->oyear, "OYEAR", $extra);

        //模試グループリスト
        makeMockGroupList($arg, $db, $model);

        //hidden
        $objForm->ae(createHiddenAe("cmd"));

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if ($model->cmd == "changeOyear") {
            $arg["reload"] = "window.open('knjh333index.php?cmd=sel&init=1', 'right_frame')";
        }

        View::toHTML($model, "knjh333Form1.html", $arg); 
    }
}

//権限チェック
function authCheck(&$arg)
{
    if (AUTHORITY != DEF_UPDATABLE) {
        $arg["jscript"] = "OnAuthError();";
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $blank = "")
{
    $result = $db->query($query);
    $opt = array();

    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                       "value" => "");
    }

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
    }
    if ($name == "OYEAR") {
        $value = ($value) ? $value : CTRL_YEAR;
    } else {
        $value = ($value) ? $value : $opt[0]["value"];
    }

    $arg[$name] = createCombo($objForm, $name, $value, $opt, $extra, 1);
}

//模試グループリスト
function makeMockGroupList(&$arg, $db, $model)
{
    $result = $db->query(knjh333Query::GetGroupName($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $mainData = "<tr> ";
        $link = View::alink("knjh333index.php",
                            $row["GROUPNAME"],
                            "target=right_frame",
                            array("cmd"     => "sel",
                                  "RYEAR"   => $model->ryear,
                                  "OYEAR"   => $model->oyear,
                                  "GROUPCD" => $row["GROUPCD"]));
        //リンク
        $mainData .= "<td align=\"left\" width=\"13%\" bgcolor=\"#ffffff\" nowrap align=\"center\" >".$link."</td>";

        //模試グループ設定
        $mockData = setListData($db, $model, "0", $row["GROUPCD"]);

        //目標値グループ設定
        $staffMock = setListData($db, $model, "1", $row["GROUPCD"], "LAST");

        $arg["data"][]["MAINLIST"] = $mainData.$mockData.$staffMock;
    }
    $result->free();
}

//明細リストセット
function setListData($db, $model, $target, $groupcd, $last = "")
{
    $rtnData = "";
    $resultMdata = $db->query(knjh333Query::getGroupData($model, $target, $groupcd));
    $rtnData .= "<td width=\"20%\" bgcolor=\"#ffffff\" nowrap>";
    while ($arow = $resultMdata->fetchRow(DB_FETCHMODE_ASSOC)) {
        $rtnData .= $arow["MOCKNAME"]."<br> ";
    }
    $resultMdata->free();

    $rtnData .= ($last == "LAST") ? "</tr>" : "" ;

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
