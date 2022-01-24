<?php

require_once('for_php7.php');

class knjf050Form1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjf050index.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $arg["schkind"] = "1";
            $query = knjf050Query::getSchkind($model);
            $extra = "onchange=\"return btn_submit('main');\"";
            makeCmb($objForm, $arg, $db, $query, "SCHKIND", $model->fields["SCHKIND"], $extra, 1);
        }

        //都道府県名取得
        $pref_name = $db->getOne(knjf050Query::getPrefName($model));
        $arg["data"]["PREF_NAME"] = ($pref_name == "") ? '本 県' : $pref_name;

        $hommehtml = "";        //男子HTML初期化
        $femmehtml = "";        //女子HTML初期化

        //性別名称取得
        $man = common::GetMasterData("SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z002' AND NAMECD2 = '1' ");
        $woman = common::GetMasterData("SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z002' AND NAMECD2 = '2' ");

        //学年数
        $grade_cnt = get_count($db->getCol(knjf050Query::getGrade($model)));

        //最小学年
        $grade_min = min($db->getCol(knjf050Query::getGrade($model)));

        $query = knjf050Query::getGrade($model);
        $result1 = $db->query($query);
        while($Row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $grade = ltrim($Row1["GRADE"],"0");

            if($model->flg == "ok" && $model->warning == ""){
                $query = knjf050Query::getBodyMeasured_Avg_Dat($Row1["GRADE"]);
                $result2 = $db->query($query);
                while($Row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC))
                {
                    if($Row2["DISTRICT"] == 1){
                        //本校男子
                        $data["HEIGHT1"][$grade][1]     = $Row2["HEIGHT1"];
                        $data["WEIGHT1"][$grade][1]     = $Row2["WEIGHT1"];
                        $data["SITHEIGHT1"][$grade][1]  = $Row2["SITHEIGHT1"];
                        //本校女子
                        $data["HEIGHT2"][$grade][1]     = $Row2["HEIGHT2"];
                        $data["WEIGHT2"][$grade][1]     = $Row2["WEIGHT2"];
                        $data["SITHEIGHT2"][$grade][1]  = $Row2["SITHEIGHT2"];
                    } elseif($Row2["DISTRICT"] == 2){
                        //本県男子
                        $data["HEIGHT1"][$grade][2]     = $Row2["HEIGHT1"];
                        $data["WEIGHT1"][$grade][2]     = $Row2["WEIGHT1"];
                        $data["SITHEIGHT1"][$grade][2]  = $Row2["SITHEIGHT1"];
                        //本県女子
                        $data["HEIGHT2"][$grade][2]     = $Row2["HEIGHT2"];
                        $data["WEIGHT2"][$grade][2]     = $Row2["WEIGHT2"];
                        $data["SITHEIGHT2"][$grade][2]  = $Row2["SITHEIGHT2"];
                    } elseif($Row2["DISTRICT"] == 3){
                        //全国男子
                        $data["HEIGHT1"][$grade][3]     = $Row2["HEIGHT1"];
                        $data["WEIGHT1"][$grade][3]     = $Row2["WEIGHT1"];
                        $data["SITHEIGHT1"][$grade][3]  = $Row2["SITHEIGHT1"];
                        //全国女子
                        $data["HEIGHT2"][$grade][3]     = $Row2["HEIGHT2"];
                        $data["WEIGHT2"][$grade][3]     = $Row2["WEIGHT2"];
                        $data["SITHEIGHT2"][$grade][3]  = $Row2["SITHEIGHT2"];
                    }
                }
            } elseif(VARS::post("cmd") == "total"){
                $result = $db->query(knjf050Query::ThisSchool_Total($Row1["GRADE"]));
                $i = 1;
                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
                {
                    //本校男子
                    if($row["SEX"] == 1){
                        $data["HEIGHT1"][$grade][1]     = $row["AVG_HEIGHT"];
                        $data["WEIGHT1"][$grade][1]     = $row["AVG_WEIGHT"];
                        $data["SITHEIGHT1"][$grade][1]  = $row["AVG_SITHEIGHT"];
                    }
                    //本校女子
                    if($row["SEX"] == 2){
                        $data["HEIGHT2"][$grade][1]     = $row["AVG_HEIGHT"];
                        $data["WEIGHT2"][$grade][1]     = $row["AVG_WEIGHT"];
                        $data["SITHEIGHT2"][$grade][1]  = $row["AVG_SITHEIGHT"];
                    }
                    $i++;
                }
                $result->free();
                $query = knjf050Query::getBodyMeasured_Avg_Dat($Row1["GRADE"]);
                $result = $db->query($query);
                while($Row = $result->fetchRow(DB_FETCHMODE_ASSOC))
                {
                    if($Row["DISTRICT"] == 2){
                        //本県男子
                        $data["HEIGHT1"][$grade][2]     = $Row["HEIGHT1"];
                        $data["WEIGHT1"][$grade][2]     = $Row["WEIGHT1"];
                        $data["SITHEIGHT1"][$grade][2]  = $Row["SITHEIGHT1"];
                        //本県女子
                        $data["HEIGHT2"][$grade][2]     = $Row["HEIGHT2"];
                        $data["WEIGHT2"][$grade][2]     = $Row["WEIGHT2"];
                        $data["SITHEIGHT2"][$grade][2]  = $Row["SITHEIGHT2"];
                    } elseif($Row["DISTRICT"] == 3){
                        //全国男子
                        $data["HEIGHT1"][$grade][3]     = $Row["HEIGHT1"];
                        $data["WEIGHT1"][$grade][3]     = $Row["WEIGHT1"];
                        $data["SITHEIGHT1"][$grade][3]  = $Row["SITHEIGHT1"];
                        //全国女子
                        $data["HEIGHT2"][$grade][3]     = $Row["HEIGHT2"];
                        $data["WEIGHT2"][$grade][3]     = $Row["WEIGHT2"];
                        $data["SITHEIGHT2"][$grade][3]  = $Row["SITHEIGHT2"];
                    }
                }

                $arg["data"]["MESSAGE"] = '※ 本校集計後は更新して下さい。';

            } elseif(VARS::post("cmd") == "update"){
                $data =& $model->fields;
            }

            //HTML作成
            if($grade == $grade_min){
                $hommehtml  .= "<tr class=\"no_search\" align=\"center\"><td rowspan=\"".$grade_cnt."\" width=\"100\">".$man["NAME1"]."</td><td width=\"60\">".$Row1["GRADE_NAME1"]."</td>";
                $femmehtml  .= "<tr class=\"no_search\" align=\"center\"><td rowspan=\"".$grade_cnt."\" width=\"100\">".$woman["NAME1"]."</td><td width=\"60\">".$Row1["GRADE_NAME1"]."</td>";
            } else {
                $hommehtml  .= "<tr class=\"no_search\" align=\"center\" width=\"60\"><td>".$Row1["GRADE_NAME1"]."</td>";
                $femmehtml  .= "<tr class=\"no_search\" align=\"center\" width=\"60\"><td>".$Row1["GRADE_NAME1"]."</td>";
            }
            for ($i=1; $i<=3; $i++)
            {
                //男子身長
                $objForm->ae( array("type"      =>  "text",
                                    "name"      =>  "HEIGHT1".$grade.$i,
                                    "value"     =>  sprintf("%0.1f",$data["HEIGHT1"][$grade][$i]),
                                    "size"      =>  "5",
                                    "maxlength" =>  "5",
                                    "extrahtml" =>  "STYLE=\"text-align: right\" onblur=\"return Num_Check(this);\""));
                $arg["data"]["HEIGHT1".$grade.$i] = $objForm->ge("HEIGHT1".$grade.$i);

                //女子身長
                $objForm->ae( array("type"      =>  "text",
                                    "name"      =>  "HEIGHT2".$grade.$i,
                                    "value"     =>  sprintf("%0.1f",$data["HEIGHT2"][$grade][$i]),
                                    "size"      =>  "5",
                                    "maxlength" =>  "5",
                                    "extrahtml" =>  "STYLE=\"text-align: right\" onblur=\"return Num_Check(this);\""));
                $arg["data"]["HEIGHT2".$grade.$i] = $objForm->ge("HEIGHT2".$grade.$i);

                //男子体重
                $objForm->ae( array("type"      =>  "text",
                                    "name"      =>  "WEIGHT1".$grade.$i,
                                    "value"     =>  sprintf("%0.1f",$data["WEIGHT1"][$grade][$i]),
                                    "size"      =>  "5",
                                    "maxlength" =>  "5",
                                    "extrahtml" =>  "STYLE=\"text-align: right\" onblur=\"return Num_Check(this);\""));
                $arg["data"]["WEIGHT1".$grade.$i] = $objForm->ge("WEIGHT1".$grade.$i);

                //女子体重
                $objForm->ae( array("type"      =>  "text",
                                    "name"      =>  "WEIGHT2".$grade.$i,
                                    "value"     =>  sprintf("%0.1f",$data["WEIGHT2"][$grade][$i]),
                                    "size"      =>  "5",
                                    "maxlength" =>  "5",
                                    "extrahtml" =>  "STYLE=\"text-align: right\" onblur=\"return Num_Check(this);\""));
                $arg["data"]["WEIGHT2".$grade.$i] = $objForm->ge("WEIGHT2".$grade.$i);

                //男子座高
                $objForm->ae( array("type"      =>  "text",
                                    "name"      =>  "SITHEIGHT1".$grade.$i,
                                    "value"     =>  sprintf("%0.1f",$data["SITHEIGHT1"][$grade][$i]),
                                    "size"      =>  "5",
                                    "maxlength" =>  "5",
                                    "extrahtml" =>  "STYLE=\"text-align: right\" onblur=\"return Num_Check(this);\""));
                $arg["data"]["SITHEIGHT1".$grade.$i] = $objForm->ge("SITHEIGHT1".$grade.$i);

                //女子座高
                $objForm->ae( array("type"      =>  "text",
                                    "name"      =>  "SITHEIGHT2".$grade.$i,
                                    "value"     =>  sprintf("%0.1f",$data["SITHEIGHT2"][$grade][$i]),
                                    "size"      =>  "5",
                                    "maxlength" =>  "5",
                                    "extrahtml" =>  "STYLE=\"text-align: right\" onblur=\"return Num_Check(this);\""));
                $arg["data"]["SITHEIGHT2".$grade.$i] = $objForm->ge("SITHEIGHT2".$grade.$i);

                $tmp = ($grade%2);
                $tmp == 0 ? $colorhtml = "#ccffcc" : $colorhtml = "#ffffff";
                //男子HTML
                $hommehtml .= "<td bgcolor=\"".$colorhtml."\" width=\"60\">".$arg["data"]["HEIGHT1".$grade."$i"]."</td>";
                $hommehtml .= "<td bgcolor=\"".$colorhtml."\" width=\"60\">".$arg["data"]["WEIGHT1".$grade."$i"]."</td>";
                $hommehtml .= "<td bgcolor=\"".$colorhtml."\" width=\"*\">".$arg["data"]["SITHEIGHT1".$grade."$i"]."</td>";
                //女子HTML
                $femmehtml .= "<td bgcolor=\"".$colorhtml."\" width=\"60\">".$arg["data"]["HEIGHT2".$grade."$i"]."</td>";
                $femmehtml .= "<td bgcolor=\"".$colorhtml."\" width=\"60\">".$arg["data"]["WEIGHT2".$grade."$i"]."</td>";
                $femmehtml .= "<td bgcolor=\"".$colorhtml."\" width=\"*\">".$arg["data"]["SITHEIGHT2".$grade."$i"]."</td>";
            }
            $hommehtml .= "</tr>";
            $femmehtml .= "</tr>";
        }

        $arg["data"]["HOMME"] = $hommehtml;     //男子HTML
        $arg["data"]["FEMME"] = $femmehtml;     //女子HTML

        //ボタン作成
        makeBtn($objForm, $arg);

        //hiddenを作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す
        View::toHTML($model, "knjf050Form1.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //本校集計ボタン
    $arg["button"]["btn_total"] = knjCreateBtn($objForm, "total", "本校集計", "onClick=\"return btn_submit('total');\"");
    //更新ボタンを作成する
    $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT) ? "onClick=\"return btn_submit('update');\"" : "disabled";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "update", "更 新", $extra);
    //取消ボタンを作成する
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", "onClick=\"return btn_submit('reset');\"");
    //終了ボタンを作成する
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}
?>