<?php

require_once('for_php7.php');

class knjz030Form1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"] = $objForm->get_start("main", "POST", "knjz030index.php", "", "main");
        $db = Query::dbCheckOut();

        $row = array();
        
        //学期数の取得(学年末分も含める）
        $model->semescount = ((int)$db->getOne(knjz030Query::getSemesterCount($model->year, $model))) + 1;
        
        //年度コンボボックス
        $opt = array();
        $result = $db->query(knjz030Query::getYears());
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["YEAR"], "value" => $row["YEAR"]);
        }
        //コンボボックス
        $extra = "onchange=\"return btn_submit('main');\"";
        $arg["YEAR"] = knjCreateCombo($objForm, "year", $model->year, $opt, $extra, 1);
        
        $i = 1;
        $query = knjz030Query::getSemester($model->year);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");

            $counter = $i++;

            if (isset($model->warning)) {
                $row["SDATE"] = $model->SDATE["$counter"];
                $row["EDATE"] = $model->EDATE["$counter"];
                $row["SEMESTERNAME"] = $model->SEMESTERNAME["$counter"];
            }

            //カレンダー
            if ($row["SEMESTER"] == "9") {
                $row["SDATE"]=str_replace("-", "/", $row["SDATE"]);
                $row["EDATE"]=str_replace("-", "/", $row["EDATE"]);

                knjCreateHidden($objForm, "SDATE".$counter, str_replace("-", "/", $row["SDATE"]));
                knjCreateHidden($objForm, "EDATE".$counter, str_replace("-", "/", $row["EDATE"]));
            } else {
                $row["SDATE"]=View::popUpCalendar($objForm, "SDATE".$counter, str_replace("-", "/", $row["SDATE"]));
                $row["EDATE"]=View::popUpCalendar($objForm, "EDATE".$counter, str_replace("-", "/", $row["EDATE"]));
            }

            //学期名称
            $extra = "";
            $row["SEMESTERNAME"] = knjCreateTextBox($objForm, $row["SEMESTERNAME"], "SEMESTERNAME".$counter, 10, 15, $extra);

            //SEMESTER(hidden)
            knjCreateHidden($objForm, "SEMESTER".$counter, $row["SEMESTER"]);

            $arg["data"][] = $row;
        }

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["BTN_UPDATE"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //クリアボタンを作成する
        $extra = "onclick=\"return btn_submit('');\"";
        $arg["button"]["BTN_CLEAR"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra, "reset");

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["BTN_END"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //学年別学期登録ボタン
        $getCount = $db->getOne(knjz030Query::getSemester($model->year, "COUNT"));
        if ($getCount == 0) {
            $disabled = "diabled";
        } else {
            $disabled = "";
        }
        $link  = REQUESTROOT."/Z/KNJZ032/knjz032index.php?mode=1&SEND_PRGID="."KNJZ030"."&SEND_YEAR=$model->year";
        $link .= "&URL_SCHOOLKIND={$model->urlSchoolKind}";
        $link .= "&URL_SCHOOLCD={$model->urlSchoolCd}";
        $link .= "&MN_ID={$model->mnId}";
        //学年別学期登録ボタン表示のプロパティ
        if ($model->Properties["useSemesterGradeMst"] == '1') {
            $arg["useSemesterGradeMst"] = 1;
        }
        $extra = "onclick=\"document.location.href='$link'\".$disabled";
        $arg["button"]["BTN_SEM_GRADE"] = knjCreateBtn($objForm, "btn_semester_grade", " 学年別学期登録 ", $extra);

        //セキュリティチェック
        $arg["close"] ="";
        if ($model->sec_competence != DEF_UPDATABLE) {
            $arg["close"] = " closing_window(); " ;
        }

        //hidden
        knjCreateHidden($objForm, "cmd");

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz030Form1.html", $arg);
    }
}
