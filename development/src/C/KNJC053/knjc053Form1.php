<?php

require_once('for_php7.php');

class knjc053Form1
{
    public function main(&$model)
    {
        $objForm = new form();

        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjc053Form1", "POST", "knjc053index.php", "", "knjc053Form1");

        $opt=array();

        if (isset($model->checked_attend)) {
            $db = Query::dbCheckOut();
            $query = knjc053Query::SQLGet_Main($model);
            //教科、科目、クラス取得
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $checked = (is_array($model->checked_attend) && in_array($row["ATTENDCLASSCD"], $model->checked_attend))? true:false;
                if ($checked==true) {
                    $grade_hr_class = $row["TARGETCLASS"];
                    $start_date = str_replace("-", "/", $row["STARTDAY"]);
                    $end_date   = str_replace("-", "/", $row["ENDDAY"]);
                    $attend_class_cd = $row["ATTENDCLASSCD"];
                    $subclass_name = $row["SUBCLASSNAME"];
                    $group_cd = $row["GROUPCD"];
                    $name_show  = $row["STAFFCD"];
                    $appdate  = $row["APPDATE"];

                    $row["TERM"] = str_replace("-", "/", $row["STARTDAY"]) ."," .str_replace("-", "/", $row["ENDDAY"]);
                    $arg["data1"][] = $row;
                }
            }
            Query::dbCheckIn($db);
        } else {
            $arg["ONLOAD"] = "wopen('../../X/KNJXTOKE5/knjxtoke5index.php?DISP=CLASS&ATTENDCLASSCD=$attend_class_cd&PROGRAMID=$model->programid','KNJXTOKE5',0,0,900,550);";
        }

        //学習記録エクスプローラー
        $objForm->ae(array("type"       => "button",
                            "name"      => "btn_toukei",
                            "value"     => "･･･",
                            "extrahtml" => "onclick=\"wopen('../../X/KNJXTOKE5/knjxtoke5index.php?DISP=CLASS&ATTENDCLASSCD=$attend_class_cd&PROGRAMID=$model->programid','KNJXTOKE5',0,0,900,550);\""));

        $arg["explore"] = $objForm->ge("btn_toukei");


        //対象クラステキストボックス作成
        $objForm->ae(array("type"       => "text",
                            "name"       => "GRADE_HR_CLASS",
                            "size"       => "10",
                            "value"      => $grade_hr_class,
                            "extrahtml"  => "readonly"));

        $arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");

        //印刷範囲
        $db = Query::dbCheckOut();
        $query = knjc053Query::SQLGet_Date($model, $attend_class_cd);
        //日付取得
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->sdate = str_replace("-", "/", $row["STARTDAY"]);
            $model->edate = str_replace("-", "/", $row["ENDDAY"]);
        }
        Query::dbCheckIn($db);

        if (!$model->sdate) {
            $model->sdate = $model->control["学籍処理日"];
        }
        if (!$model->sdate) {
            $model->edate = $model->control["学籍処理日"];
        }

        $arg["data"]["DATE"]  = View::popUpCalendar($objForm, "DATE", $model->sdate);
        $arg["data"]["DATE2"] = View::popUpCalendar($objForm, "DATE2", $model->edate);

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "STARTDAY",
                            "value"     => $model->sdate
                            ));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "ENDDAY",
                            "value"     => $model->edate
                            ));

        //科目名表示
        $arg["data"]["SUBCLASSNAME"] = $subclass_name;

        //実施期間表示
        if (isset($model->sdate)) {
            $kara = "&nbsp;～&nbsp;";
        } else {
            $kara = "";
        }
        $arg["data"]["STARTENDDAY"] = $model->sdate.$kara.$model->edate;
        /*
                //空行チェックボックスを作成する
                $objForm->ae( array("type"       => "checkbox",
                                    "name"       => "OUTPUT3",
                                    "extrahtml"  => "checked",
                                    "value"      => isset($model->field["OUTPUT3"])?$model->field["OUTPUT3"]:"1"));

                $arg["data"]["OUTPUT3"] = $objForm->ge("OUTPUT3");
        */
        //集計票チェックボックス
        $objForm->ae(array("type"      => "checkbox",
                            "name"      => "OUTPUT4",
                            "checked"   => true,
                            "extrahtml"	=> "id=\"OUTPUT4\"",
                            "value"     => isset($model->field["OUTPUT4"])?$model->field["OUTPUT4"]:"1"));

        $arg["data"]["OUTPUT4"] = $objForm->ge("OUTPUT4");

        //明細票チェックボックス
        $objForm->ae(array("type"       => "checkbox",
                            "name"       => "OUTPUT5",
                            "checked"    => true,
                            "extrahtml"	=> "id=\"OUTPUT5\"",
                            "value"      => isset($model->field["OUTPUT5"])?$model->field["OUTPUT5"]:"1"));

        $arg["data"]["OUTPUT5"] = $objForm->ge("OUTPUT5");

        //注意・超過のタイトル
        $arg["data"]["TYUI_TYOUKA_TITLE"] = "欠課数上限値（履修／修得）";

        //注意・超過
        $opt = array(1, 2); //1:注意 2:超過
        $model->field["TYUI_TYOUKA"] = ($model->field["TYUI_TYOUKA"] == "") ? "1" : $model->field["TYUI_TYOUKA"];
        $extra = array("id=\"TYUI_TYOUKA1\"", "id=\"TYUI_TYOUKA2\"");
        $radioArray = knjCreateRadio($objForm, "TYUI_TYOUKA", $model->field["TYUI_TYOUKA"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //hiddenを作成する
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "OUTPUT2",
                            "value"     => "1"
                            ));

        //印刷ボタンを作成する
        $objForm->ae(array("type" => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return opener_submit('" . SERVLET_URL . "');\"" ));

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");

        //終了ボタンを作成する
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ));

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"      => DB_DATABASE,
                            ));

        //hiddenを作成する
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJC053"
                            ));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "cmd"
                            ));

        //年度データ
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"     => $model->control["年度"]
                            ));

        $arg["data"]["YEAR"] = $model->control["年度"];

        //学期名表示
        $arg["data"]["SEME_NAME"] = $model->control["学期名"][$model->semester];     //学期名

        //学期用データ
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "SEMESTER",
                            "value"     => $model->semester
                            ));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "CLASSCD",
                            "value"     => $model->classcd
                            ));

        knjCreateHidden($objForm, "SCHOOL_KIND", $model->school_kind);
        knjCreateHidden($objForm, "CURRICULUM_CD", $model->curriculum_cd);

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "SUBCLASSCD",
                            "value"     => $model->subclasscd
                            ));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "chikokuHyoujiFlg",
                            "value"     => $model->Properties["chikokuHyoujiFlg"]
                            ));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "useTestCountflg",
                            "value"     => $model->Properties["useTestCountflg"]
                            ));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "ATTENDCLASSCD",
                            "value"     => $attend_class_cd
                            ));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "GROUPCD",
                            "value"     => $group_cd
                            ));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "NAME_SHOW",
                            "value"     => $name_show
                            ));
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "APPDATE",
                            "value"     => $appdate
                            ));

        //教育課程対応
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
        knjCreateHidden($objForm, "useTestCountflg", $model->Properties["useTestCountflg"]);
        knjCreateHidden($objForm, "use_SchregNo_hyoji", $model->Properties["use_SchregNo_hyoji"]);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "knjc053useMeisaiExecutedate", $model->Properties["knjc053useMeisaiExecutedate"]);

        //タイトル
        $arg["TITLE"] = $model->title;

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjc053Form1.html", $arg);
    }
}
