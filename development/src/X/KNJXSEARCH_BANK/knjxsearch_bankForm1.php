<?php

require_once('for_php7.php');

class knjxsearch_bankForm1 {
    function main(&$model) {
        //権限チェック
        $auth = common::SecurityCheck(STAFFCD, $model->programid);
        if ($auth != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        $arg["start"] = $objForm->get_start("knjxsearch_bankForm1", "POST", "knjxsearch_bankindex.php", "", "knjxsearch_bankForm1");

        $db     = Query::dbCheckOut();

        $arg["EXP_YEAR"] = "&nbsp;年度：" .CTRL_YEAR ."&nbsp;&nbsp;学期：" .CTRL_SEMESTERNAME;


        //学年コンボボックス
        $opt = array();
        if ($model->cmd == "search"){
            $opt[] = array("label"  => '',
                            "value" => '');
        }
        
        $result = $db->query(knjxsearch_bankQuery::GetHr_Class($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label"  => $row["HR_NAME"],
                           "value" => $row["GRADE"]."-".$row["HR_CLASS"]);
        }
        $model->grade = $model->grade ? $model->grade : $opt[0]["value"];
        $value = $model->cmd == "search" ? $model->search_fields["HR_CLASS"] : $model->grade;

        $extra = " onChange=\"return btn_submit('chg_grade')\"";
        
        $arg["GRADE"] =  knjCreateCombo($objForm, "GRADE", $value, $opt, $extra, "1");

        //検索ボタン
        $objForm->ae( array("type" 		=> "button",
                            "name"      => "SEARCH_BTN",
                            "value"     => "検索",
                            "extrahtml" => "onclick=\"wopen('knjxsearch_bankindex.php?cmd=search_view','knjxsearch_bank',0,0,600,400);\""));
        $arg["SEARCH_BTN"] = $objForm->ge("SEARCH_BTN");

        //学校区分抽出
        $schooldiv=="";
        if (isset($model->search_fields["graduate_year"])) {
            $result = $db->query(knjxsearch_bankQuery::GetSchoolDiv($model->search_fields["graduate_year"]));
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                 $schooldiv = $row["SCHOOLDIV"];
            }
        }
        if ($schooldiv=="") $schooldiv = $model->control["学校区分"];

        //検索結果表示
        $result = $db->query(knjxsearch_bankQuery::SearchStudent($model,$model->search_fields,$schooldiv));
        $image  = array(REQUESTROOT ."/image/system/boy1.gif", REQUESTROOT ."/image/system/girl1.gif");
        $i =0;
        list($path, $cmd) = explode("?cmd=", $model->path);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $a = array("cmd"    => $cmd,
                      "SCHREGNO"    => $row["SCHREGNO"],
                      "GRADE"       => $row["GRADE"],
                      "HR_CLASS"    => $row["HR_CLASS"],
                      "ATTENDNO"    => $row["ATTENDNO"],
                      "NAME"        => $row["NAME_SHOW"]);

            $row["NAME_SHOW"] = View::alink(REQUESTROOT .$path, htmlspecialchars($row["NAME_SHOW"]), "target=".$model->target, $a);
            $row["IMAGE"] = $image[($row["SEX"]-1)];
            $row["ATTENDNO"] = $row["HR_NAMEABBV"]."-".$row["ATTENDNO"];
            $arg["data"][] = $row;
            $i++;
        }
        $arg["RESULT"] = "結果：".$i."名";
        $result->free();
        if ($i == 0 && $model->cmd == "search") {
            $arg["search_result"] = "SearchResult();";
        }

        Query::dbCheckIn($db);

        $objForm->add_element(array("type"      => "checkbox",
                                    "name"      => "chk_all",
                                    "extrahtml"   => "onClick=\"return check_all();\"" ));

        //hidden(検索条件値を格納する)
        knjCreateHidden($objForm, "HR_CLASS");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SRCH_SCHREGNO");
        knjCreateHidden($objForm, "NAME");
        knjCreateHidden($objForm, "NAME_SHOW");
        knjCreateHidden($objForm, "NAME_KANA");
        knjCreateHidden($objForm, "NAME_ENG");
        knjCreateHidden($objForm, "BANKCD");
        knjCreateHidden($objForm, "BRANCHCD");
        knjCreateHidden($objForm, "DEPOSIT_ITEM");
        knjCreateHidden($objForm, "ACCOUNTNO");
        
        if ($model->cmd == "search" || $model->cmd == "chg_grade") {
            $arg["reload"] = "parent.right_frame.location.href = '/deve_kinh/P/KNJP040K/knjp040kindex.php?cmd=edit&init=1';";
        }

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjxsearch_bankForm1.html", $arg);
    }
}
?>
