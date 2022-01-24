<?php

require_once('for_php7.php');

class knja210Form1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knja210Form1", "POST", "knja210index.php", "", "knja210Form1");
        $opt=array();
        //カレンダーコントロール
        $arg["el"]["DATE"]=View::popUpCalendar($objForm,"DATE",isset($model->field["DATE"])?$model->field["DATE"]:$model->control["学籍処理日"]);

        if (isset($model->checked_attend)) {
            $db = Query::dbCheckOut();
            $query = knja210Query::SQLGet_Main($model);
            //教科、科目、クラス取得
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $checked = (is_array($model->checked_attend) && in_array($row["ATTENDCLASSCD"], $model->checked_attend))? true:false;
                if($checked==true) {
                    $grade_hr_class  = $row["TARGETCLASS"];
                    $start_date      = $row["STARTDAY"];
                    $end_date        = $row["ENDDAY"];
                    $attend_class_cd = $row["ATTENDCLASSCD"];
                    $staffname_show  = $row["STAFFNAME_SHOW"];
                    $name_show       = $row["STAFFCD"];
                    $appdate         = $row["APPDATE"];
                    $grade           = $row["GRADE"];
                    $hr_class        = $row["HR_CLASS"];
                    $group_cd        = $row["GROUPCD"];
                    $subclass_name   = $row["SUBCLASSNAME"];

                    $row["TERM"] = str_replace("-","/",$row["STARTDAY"]) ."," .str_replace("-","/",$row["ENDDAY"]);
                    $arg["data1"][] = $row;
                }
            }
            Query::dbCheckIn($db);
        } else {
            $arg["ONLOAD"] = "wopen('../../X/KNJXTOKE5/knjxtoke5index.php?DISP=CLASS&ATTENDCLASSCD=$attend_class_cd&PROGRAMID=$model->programid','KNJXTOKE5',0,0,900,550);";
        }

        //対象クラステキストボックス作成
        $objForm->ae( array("type"       => "text",
                            "name"       => "GRADE_HR_CLASS",
                            "size"       => "10",
                            "value"      => $grade_hr_class,
                            "extrahtml"  => "readonly"));

        $arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");

        //読込ボタンを作成（学習記録エクスプローラを表示する）
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_toukei",
                            "value"       => "･･･",
                            "extrahtml"   => "onclick=\"wopen('../../X/KNJXTOKE5/knjxtoke5index.php?DISP=CLASS&ATTENDCLASSCD=$attend_class_cd&PROGRAMID=$model->programid','KNJXTOKE5',0,0,900,550);\"") );

        $arg["explore"] = $objForm->ge("btn_toukei");

        //科目名表示
        $arg["data"]["SUBCLASSNAME"] = $subclass_name;

        //実施期間表示
        if(isset($start_date)) {
            $kara = "&nbsp;～&nbsp;";
        } else {
            $kara = "";
        }
        $arg["data"]["STARTENDDAY"] = str_replace("-","/",$start_date).$kara.str_replace("-","/",$end_date);

        //フォーム選択 (1:5列×5行 2:6列×7行)
        $opt = array(1, 2);
        $model->field["FORM_SENTAKU"] = ($model->field["FORM_SENTAKU"] == "") ? "1" : $model->field["FORM_SENTAKU"];
        $extra = array("id=\"FORM_SENTAKU1\"", "id=\"FORM_SENTAKU2\"");
        $radioArray = knjCreateRadio($objForm, "FORM_SENTAKU", $model->field["FORM_SENTAKU"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //学籍番号を出力する
        $extra = "checked='checked' id='PRINT_SCHREGNO' ";
        $arg["data"]["PRINT_SCHREGNO"] = knjCreateCheckBox($objForm, "PRINT_SCHREGNO", 1, $extra);

        //学期名表示
        $arg["data"]["SEME_NAME"] = $model->control["学期名"][$model->semester]; //学期名

        //印刷ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");

        //終了ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");


        //hiddenを作成する(必須)/////////////////////////////////////////////////////////////////////////////////////////////
        
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        //データベース名
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"      => DB_DATABASE
                            ) );

        //プログラムＩＤ
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJA210"
                            ) );

        //写真データ取得で使用
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DOCUMENTROOT",
                            "value"     => DOCUMENTROOT
                            ) );

        //年度
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"     => $model->control["年度"]
                            ) );

        $arg["data"]["YEAR"] = $model->control["年度"];

        //学期
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SEMESTER",
                            "value"     => $model->semester
                            ) );

        //学期
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GRADE",
                            "value"     => $grade
                            ) );

        //学期
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "HR_CLASS",
                            "value"     => $hr_class
                            ) );

        //有効期限
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "STARTDAY",
                            "value"     => $start_date
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ENDDAY",
                            "value"     => $end_date
                            ) );

        //科目担任名
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "NAME_SHOW",
                            "value"     => $name_show
                            ) );
        //2004-08-11 naka
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "APPDATE",
                            "value"     => $appdate
                            ) );

        //受講クラスコード
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ATTENDCLASSCD",
                            "value"     => $attend_class_cd
                            ) );

        //学期開始終了日
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "TESTKINDCD",
                            "value"     => $model->testkindcd
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "TESTITEMCD",
                            "value"     => $model->testitemcd
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "CLASSCD",
                            "value"     => $model->classcd
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SUBCLASSCD",
                            "value"     => $model->subclasscd
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GROUPCD",
                            "value"     => $group_cd
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DISP"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        //送信先のGET変数をセット
        $aHash = array("YEAR"             =>  $model->year,         //年度
                        "SEMESTER"        =>  $model->semester,     //学期
                        "CLASSCD"         =>  $model->classcd,      //学科コード
                        "LNAME_SHOW"      =>  $model->lname_show,   //学科コード
                        "FNAME_SHOW"      =>  $model->fname_show,   //学科コード
                        "SUBCLASSCD"      =>  (is_array($model->subclasscd))? implode($model->subclasscd,',') : "",   //科目コード
                        "TESTKINDCD"      =>  $model->testkindcd,   //テスト種別コード
                        "TESTITEMCD"      =>  $model->testitemcd,   //テスト項目コード
                        "ATTENDCLASSCD"   =>  (is_array($model->checked_attend))? implode($model->checked_attend,',') : "", //受講クラスコード
                        "GROUPCD"         =>  (is_array($model->groupcd))? implode($model->groupcd,',') : ""          //郡コード
                        );

        $href .= "";
        $arrParam = array();
        foreach($aHash as $key => $val ) {
            $arrParam[] = urlencode($key) . "=" . urlencode($val);
        }
        $href .= join( '&', $arrParam );

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "href",
                            "value"     => $href
                            ) );

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja210Form1.html", $arg);
    }
}
?>
