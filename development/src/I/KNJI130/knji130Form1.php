<?php

require_once('for_php7.php');

class knji130Form1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knji130index.php", "", "list");

        //DB接続
        $db = Query::dbCheckOut();

        //権限チェック
        if (AUTHORITY < DEF_UPDATE_RESTRICT) {
            $arg["Closing"] = " closing_window(1);";
        }
        //事前処理チェック
        if (!knji130Query::checktoStart($db)) {
            $arg["Closing"] = " closing_window(2);";
        }

        //検索条件表示
        $arg["GRD_YEAR"]        = $model->search["GRD_YEAR"];
        $arg["GRADE_HR_CLASS"]  = ($model->search["GRADE_HR_CLASS"]) ? $db->getOne(knji130Query::searchGradeHrClass($model, $model->search["GRD_YEAR"], $model->search["GRADE_HR_CLASS"])) : "";
        $arg["COURSECODE"]      = ($model->search["COURSECODE"]) ? $db->getOne(knji130Query::searchCourseCodeMst($model->search["COURSECODE"])) : "";
        $arg["S_SCHREGNO"]      = $model->search["S_SCHREGNO"];
        $arg["NAME"]            = $model->search["NAME"];
        $arg["NAME_SHOW"]       = $model->search["NAME_SHOW"];
        $arg["NAME_KANA"]       = $model->search["NAME_KANA"];
        $arg["SEX"]             = ($model->search["SEX"]) ? $db->getOne(knji130Query::getNameMst("Z002", $model->search["SEX"], 2)) : "";

        //生徒表示
        $search = "";
        foreach ($model->search as $key) {
            $search .= $key;
        }
        if ($search) {
            $result = $db->query(knji130Query::getStudentList($model));
            $image  = array(REQUESTROOT ."/image/system/boy1.gif", REQUESTROOT ."/image/system/girl1.gif");
            $i = 0;
            $schregno = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $schregno[] = $row["SCHREGNO"];
                $a = array("cmd"            => "edit",
                           "SCHREGNO"       => $row["SCHREGNO"],
                           "EXP_YEAR"       => $row["YEAR"],
                           "EXP_SEMESTER"   => $row["SEMESTER"],
                           "GRADE"          => $row["GRADE"]);

                $row["NAME_SHOW"] = View::alink("knji130index.php", htmlspecialchars($row["NAME_SHOW"]), "target=right_frame", $a);
                $row["IMAGE"] = $image[($row["SEX"]-1)];
                $row["ATTENDNO"] = $row["HR_NAMEABBV"]."-".$row["ATTENDNO"];
                $arg["data"][] = $row;
                $i++;
            }
            $arg["CLASS_SUM"] = $i;
            $result->free();
        }

        //検索ボタン作成
        $extra = "onclick=\"showSearch()\"";
        $arg["btn_search"] = knjCreateBtn($objForm, "btn_search", "検 索", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "GRD_YEAR");
        knjCreateHidden($objForm, "GRADE_HR_CLASS");
        knjCreateHidden($objForm, "COURSECODE");
        knjCreateHidden($objForm, "S_SCHREGNO");
        knjCreateHidden($objForm, "NAME");
        knjCreateHidden($objForm, "NAME_SHOW");
        knjCreateHidden($objForm, "NAME_KANA");
        knjCreateHidden($objForm, "SEX");

        if (is_array($schregno)) {
            knjCreateHidden($objForm, "SCHREGNO", implode(",", $schregno));
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knji130Form1.html", $arg);
    }
}
