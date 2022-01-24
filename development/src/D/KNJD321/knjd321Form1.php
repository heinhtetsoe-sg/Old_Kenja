<?php

require_once('for_php7.php');


class knjd321Form1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]    = $objForm->get_start("main", "POST", "knjd321index.php", "", "main");
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;
        //学期
        $arg["GAKKI"] = CTRL_SEMESTERNAME;
        //上限値
        $query = knjd321Query::getSyuutokuJougenti($model);
        $row_jougen = $db->getRow($query, DB_FETCHMODE_ASSOC);
        //修得上限値
        $arg["SYUTOKU_JOUGENTI"] = $row_jougen["SYUTOKU_JOUGENTI"];
        //履修上限値
        $arg["RISYU_JOUGENTI"]   = $row_jougen["RISYU_JOUGENTI"];

        //ABC評定科目
        $model->hyouteiABC = array();
        $query = knjd321Query::getABCHyoutei();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $model->hyouteiABC[$row["NAME1"]] = $row["NAME1"];
        }
        $result->free();

        /******************/
        /* コンボボックス */
        /******************/
        //学期
        $opt_seme = array();
        $query = knjd321Query::getSelectSeme();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_seme[] = array('label' => $row["SEMESTERNAME"],
                                'value' => $row["SEMESTER"]);
        }
        $result->free();
        if ($model->field["GAKKI2"] == "") $model->field["GAKKI2"] = '9';
        $extra = "";
        $arg["GAKKI2"] = knjCreateCombo($objForm, "GAKKI2", $model->field["GAKKI2"], $opt_seme, $extra, 1);
        //学年
        $opt_grade = array();
        $query = knjd321Query::getSelectGrade();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_grade[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
        }
        $result->free();
        $opt_grade[] = array('label' => "全て", 'value' => "99");
        if ($model->field["GRADE"] == "") $model->field["GRADE"] = $opt_grade[0]["value"];
        $extra = "";
        $arg["GRADE"] = knjCreateCombo($objForm, "GRADE", $model->field["GRADE"], $opt_grade, $extra, 1);

        /********/
        /* 日付 */
        /********/
        //異動対象日付
        $date = isset($model->field["DATE"]) ? $model->field["DATE"] : str_replace("-", "/", CTRL_DATE);
        $arg["DATE"]=View::popUpCalendar($objForm, "DATE", $date);

        /********************/
        /* チェックボックス */
        /********************/
        //教科・科目/総合的な時間
        if (!$model->field["KYOUKA_SOUGOU1"] && !$model->field["KYOUKA_SOUGOU2"]) {
            $model->field["KYOUKA_SOUGOU1"] = '1';
        }
        $extra = ($model->field["KYOUKA_SOUGOU1"] == "1") ? $extra = "checked='checked' id=\"KYOUKA_SOUGOU1\"" : "id=\"KYOUKA_SOUGOU1\"";
        $arg["KYOUKA_SOUGOU1"] = knjCreateCheckBox($objForm, "KYOUKA_SOUGOU1", "1", $extra);
        $extra = ($model->field["KYOUKA_SOUGOU2"] == "1") ? $extra = "checked='checked' id=\"KYOUKA_SOUGOU2\"" : "id=\"KYOUKA_SOUGOU2\"";
        $arg["KYOUKA_SOUGOU2"] = knjCreateCheckBox($objForm, "KYOUKA_SOUGOU2", "1", $extra);

        //成績不振者
        $extra = ($model->field["SEISEKI_HUSIN1"] == "1" || !strlen($model->cmd)) ? "checked='checked' id=\"SEISEKI_HUSIN1\"" : "id=\"SEISEKI_HUSIN1\"";
        //$extra = "checked='checked' id=\"SEISEKI_HUSIN1\" disabled";
        $arg["SEISEKI_HUSIN1"] = knjCreateCheckBox($objForm, "SEISEKI_HUSIN1", "1", $extra);
        $extra = ($model->field["SEISEKI_HUSIN2"] == "1") ? "checked='checked' id=\"SEISEKI_HUSIN2\"" : "id=\"SEISEKI_HUSIN2\"";
        $arg["SEISEKI_HUSIN2"] = knjCreateCheckBox($objForm, "SEISEKI_HUSIN2", "1", $extra);
        $extra = ($model->field["SEISEKI_HUSIN3"] == "1") ? "checked='checked' id=\"SEISEKI_HUSIN3\"" : "id=\"SEISEKI_HUSIN3\"";
        $arg["SEISEKI_HUSIN3"] = knjCreateCheckBox($objForm, "SEISEKI_HUSIN3", "1", $extra);

        /**********/
        /* その他 */
        /**********/
        //評定 or 評価
        if ($model->field["GAKKI2"] == 9) {
            $arg["HYOUTEI_OR_HYOUKA"] = '評定';
        } else {
            $arg["HYOUTEI_OR_HYOUKA"] = '評価';
        }

        /********************/
        /* テキストボックス */
        /********************/
        //成績不振者 教科・科目
        $extra = "style=\"text-align: right\" onblur=\"this.value=toFloat(this.value)\"";
        $value = (!strlen($model->cmd)) ? '1' : $model->field["SEISEKI_HUSIN_HYOUTEI_FROM"];
        $arg["SEISEKI_HUSIN_HYOUTEI_FROM"] = knjCreateTextBox($objForm, $value, "SEISEKI_HUSIN_HYOUTEI_FROM", 3, 3, $extra);
        $value = (!strlen($model->cmd)) ? '1' : $model->field["SEISEKI_HUSIN_HYOUTEI_TO"];
        $arg["SEISEKI_HUSIN_HYOUTEI_TO"] = knjCreateTextBox($objForm, $value, "SEISEKI_HUSIN_HYOUTEI_TO", 3, 3, $extra);

        /************/
        /* 一覧表示 */
        /************/
        if ($model->cmd == 'read' || $model->cmd == 'search') {
            $dataFlg = false;
            $counter = 0;
            $colorFlg = false;
            $model->data=array();
            $query = knjd321Query::selectListQuery($model);
            $result = $db->query($query);

            if ($result->numRows() == 0 && $model->cmd == 'search') {
                $model->setMessage("MSG303","\\n学籍番号：".$model->search_schregno);
            }

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //学籍番号を配列で取得
                $subclassCd = $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"];
                $model->data["SCHREGNO"][] = $row["SCHREGNO"]."-".$subclassCd;
                //５行毎に背景色を変える
                if ($counter % 5 == 0) {
                    $colorFlg = !$colorFlg;
                }
                $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";
                //クラス-出席番(表示)
                if ($row["HR_NAME"] != "" && $row["ATTENDNO"] != "") {
                    $row["ATTENDNO"] = sprintf("%s-%02d", $row["HR_NAME"], $row["ATTENDNO"]);
                }
                //文字評定の取得
                $row["CHARASSESS"] = "";
                if ($row["GRADE_RECORD"] != "" && ($row["SUBCLASSCD"] == "900100" OR in_array($subclassCd, $model->hyouteiABC))) {
                    $row["CHARASSESS"] = $db->getOne(knjd321Query::getAssess($row["GRADE"], $subclassCd, $row["GRADE_RECORD"], $model));
                }
                $row["ASSESS_ID"] = "ASSESS_ID".$counter;
                //テキストボックスを作成
                $extra = "style=\"text-align: right\" onChange=\"this.style.background='#ccffcc'\" onblur=\"this.value=toInteger(this.value)\" ";
                $name = "DUMMY";
                if ($row["JUDGE_PATTERN"] == "A") {
                    $name = "A_PATTERN_ASSESS";
                } else 
                if ($row["JUDGE_PATTERN"] == "B") {
                    $name = "B_PATTERN_ASSESS";
                } else 
                if ($row["JUDGE_PATTERN"] == "C") {
                    $name = "C_PATTERN_ASSESS";
                } else 
                if ($row["SUBCLASSCD"] == "900100" OR in_array($subclassCd, $model->hyouteiABC)) {
                    //相対評定のデータを取得
                    $amark = $alow = $ahigh = "";
                    $result2 = $db->query(knjd321Query::selectAssessLevel($row["GRADE"], $subclassCd, $model));
                    while( $row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC))
                    {
                        $al = explode(".",$row2["ASSESSLOW"]);
                        $ah = explode(".",$row2["ASSESSHIGH"]);
                        if ($amark == "") {
                            $amark = $row2["ASSESSMARK"];
                            $alow   = $al[0];
                            $ahigh  = $ah[0];
                        } else {
                            $amark  .= "," . $row2["ASSESSMARK"];
                            $alow   .= "," . $al[0];
                            $ahigh  .= "," . $ah[0];
                        }
                    }
                    $result2->free();

                    $extra = "style=\"text-align: right\" onblur=\"calc2(this,$counter);\" onChange=\"SetAssess(this,$counter,'".$amark."','".$alow."','".$ahigh."'); this.style.background='#ccffcc'\" ";
                    $name = "GRADE_RECORD";
                }
                $row[$name] = knjCreateTextBox($objForm, $row[$name], $name."-".$counter, 3, 3, $extra);
/***
***/
                //更新チェックボックス
                $name = "UPDATE_DATA";
                $extra = "";
                $row[$name] = knjCreateCheckBox($objForm, $name."-".$counter, "1", $extra);

                $dataFlg = true;
                $counter++;
                $arg["data"][] = $row;
            }
        }


        /**********/
        /* ボタン */
        /**********/
        //在学生検索ボタンを作成する
        $param = "&S_SCH=1&S_HR=1&S_COURSE=1&S_NAME=1&S_NAMESHOW=1&S_KANA=1";
        $extra = "onclick=\"wopen('../../X/KNJXSEARCH_STUDENT/knjxSearch_Studentindex.php?PATH=/D/KNJD321/knjd321index.php&cmd=&target=KNJD321{$param}','search',0,0,700,500);\"";
        $arg["btn_search"] = knjCreateBtn($objForm, "btn_search", "在校生検索", $extra);
        //読 込
        $extra = "onclick=\"return btn_submit('read');\"";
        $arg["btn_read"] = knjCreateBtn($objForm, "btn_read", "読 込", $extra);
        //欠課時数オーバーの更新処理へ
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF") ? "disabled" : "";
        $extra = $disable." onClick=\" wopen('".REQUESTROOT."/D/KNJD321B/knjd321bindex.php?cmd=','SUBWIN2',0,0,screen.availWidth,screen.availheight);\"";
        $arg["btn_knjd321b"] = knjCreateBtn($objForm, "btn_knjd321b", "欠課時数超過者一括更新(評定&#39;0&#39;)", $extra);
        //更 新
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取 消
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終 了
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SEARCH_SCHREGNO", $model->search_schregno);

        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd321Form1.html", $arg);
    }
}
?>
