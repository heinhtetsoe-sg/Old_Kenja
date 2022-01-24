<?php

require_once('for_php7.php');

class knjc050Form1
{
    function main(&$model){

        $objForm = new form;

        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjc050Form1", "POST", "knjc050index.php", "", "knjc050Form1");

        $opt=array();



        if (isset($model->checked_attend)){
            $db = Query::dbCheckOut();
            $query = knjc050Query::SQLGet_Main($model);
//echo $query;
            //教科、科目、クラス取得
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
//                $row["TARGETCLASS"] = common::PubFncData2Print($row["TARGETCLASS"]);
                $checked = (is_array($model->checked_attend) && in_array($row["ATTENDCLASSCD"], $model->checked_attend))? true:false;
                if($checked==true) {
			
        			$grade_hr_class = $row["TARGETCLASS"];
        			$start_date = str_replace("-","/",$row["STARTDAY"]);
        			$end_date   = str_replace("-","/",$row["ENDDAY"]);
        			$attend_class_cd = $row["ATTENDCLASSCD"];
        			$subclass_name = $row["SUBCLASSNAME"];
        			$group_cd = $row["GROUPCD"];
					$name_show  = $row["STAFFCD"];	//2004-08-11 naka
					$appdate  = $row["APPDATE"];	//2004-08-11 naka
			
        		    $row["TERM"] = str_replace("-","/",$row["STARTDAY"]) ."," .str_replace("-","/",$row["ENDDAY"]);
                    $arg["data1"][] = $row;
        		}
            }
            Query::dbCheckIn($db);
        }
        else {
            $arg["ONLOAD"] = "wopen('../../X/KNJXTOKE5/knjxtoke5index.php?DISP=CLASS&ATTENDCLASSCD=$attend_class_cd&PROGRAMID=$model->programid','KNJXTOKE5',0,0,900,550);";
        }


        //学習記録エクスプローラー
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_toukei",
                            "value"       => "･･･",
                            "extrahtml"   => "onclick=\"wopen('../../X/KNJXTOKE5/knjxtoke5index.php?DISP=CLASS&ATTENDCLASSCD=$attend_class_cd&PROGRAMID=$model->programid','KNJXTOKE5',0,0,900,550);\"") );
    
        $arg["explore"] = $objForm->ge("btn_toukei");


        //対象クラステキストボックス作成
        $objForm->ae( array("type"       => "text",
                            "name"       => "GRADE_HR_CLASS",
                            "size"       => "10",
                            "value"      => $grade_hr_class,
                            "extrahtml"  => "readonly"));

        $arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");

        //印刷範囲
        $arg["data"]["DATE"] = View::popUpCalendar($objForm,"DATE",isset($start_date)?$start_date:$model->control["学籍処理日"]);
        $arg["data"]["DATE2"] = View::popUpCalendar($objForm,"DATE2",isset($end_date)?$end_date:$model->control["学籍処理日"]);

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "STARTDAY",
                            "value"     => $start_date
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ENDDAY",
                            "value"     => $end_date
                            ) );

        //科目名表示
        $arg["data"]["SUBCLASSNAME"] = $subclass_name;

        //実施期間表示
        if(isset($start_date))
        {
        	$kara = "&nbsp;～&nbsp;";
        }
        else
        {
        	$kara = "";
        }
        $arg["data"]["STARTENDDAY"] = $start_date.$kara.$end_date;

		//欠課処理チェックボックスを作成する/////////////////////////////////////////////////////////
		$objForm->ae( array("type"       => "checkbox",
                    		"name"       => "OUTPUT3",
		                    "extrahtml"  => "checked",
							"value"      => isset($model->field["OUTPUT3"])?$model->field["OUTPUT3"]:"1"));

		$arg["data"]["OUTPUT3"] = $objForm->ge("OUTPUT3");

/* 2004/07/07 delete by nakamoto
        //ラジオボタンを作成//累計種別（学期間/印刷範囲）
        $opt[0]=1;
        $opt[1]=2;
        $objForm->ae( array("type"       => "radio",
                            "name"       => "OUTPUT2",
        					"value"      => isset($model->field["OUTPUT2"])?$model->field["OUTPUT2"]:"1",
        					"multiple"   => $opt));

        $arg["data"]["OUTPUT21"] = $objForm->ge("OUTPUT2",1);
        $arg["data"]["OUTPUT22"] = $objForm->ge("OUTPUT2",2);
*/
        //hiddenを作成する 2004/07/07 add by nakamoto
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "OUTPUT2",
                            "value"     => "1"
                            ) );


        //印刷ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
		                    "extrahtml"   => "onclick=\"return opener_submit('" . SERVLET_URL . "');\"" ) );

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");

        //終了ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"      => DB_DATABASE,
                            ) );

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJC050"
//                            "value"     => $model->programid
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        //年度データ
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"     => $model->control["年度"]
                            ) );

        $arg["data"]["YEAR"] = $model->control["年度"];

        //学期名表示
        $arg["data"]["SEME_NAME"] = $model->control["学期名"][$model->semester];     //学期名

        //学期用データ
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SEMESTER",
                            "value"     => $model->semester
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
                            "name"      => "ATTENDCLASSCD",
                            "value"     => $attend_class_cd
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GROUPCD",
                            "value"     => $group_cd
                            ) );

		//2004-08-11 naka
		$objForm->ae( array("type"      => "hidden",
		                    "name"      => "NAME_SHOW",
		                    "value"     => $name_show
		                    ) );
		$objForm->ae( array("type"      => "hidden",
		                    "name"      => "APPDATE",
		                    "value"     => $appdate
		                    ) );

        //タイトル
        $arg["TITLE"] = $model->title;

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjc050Form1.html", $arg); 
    }

}
?>
