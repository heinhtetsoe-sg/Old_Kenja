<?php

require_once('for_php7.php');

class knjp140kForm3
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjp140kindex.php", "", "edit");
        $arg["reload"] = "";

        //分納コード
        $arg["INST_CD"] = $model->inst_cd;
        $db = Query::dbCheckOut();

        $opt_left = $opt_right = array();
        
        $result   = $db->query(knjp140kQuery::GetMcd($model->schregno, $model->inst_cd));
        $sum = 0;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //小分類を持っている中分類の場合、小分類の合計金額を取得
            if($row["EXPENSE_S_EXIST_FLG"] == "1") {
                $row["MONEY_DUE"] = $db->getOne(knjp140kQuery::getMoneyDue($model->schregno, $row["EXPENSE_M_CD"]));
            }

            if (strlen($row["INST_CD"]) && $row["INST_CD"] == $model->inst_cd){
                $opt_left[]  = array("label" => $row["EXPENSE_L_CD"].$row["EXPENSE_M_CD"].":".$row["EXPENSE_M_NAME"], 
                                     "value" => $row["EXPENSE_M_CD"]);
                $sum += (int)$row["MONEY_DUE"];
            } else {
                //入金済みデータが存在すれば表示しない
                $exist = $db->getOne(knjp140kQuery::PaidCehck($row));

                if (!strlen($exist)) {
                    $opt_right[] = array("label" => $row["EXPENSE_L_CD"].$row["EXPENSE_M_CD"].":".$row["EXPENSE_M_NAME"], 
                                         "value" => $row["EXPENSE_M_CD"]);
                }
            }
            //各項目の必要納入金額
            $arg["data2"][] = array("idx" => $row["EXPENSE_M_CD"], "money" => $row["MONEY_DUE"]);
        }
        
        //左リストの合計
        $arg["M_SUM"] = number_format($sum);

		//NO003
		$model->money2 = $sum;

        Query::dbCheckIn($db);

        if ($model->div == "M") {
            $arg["data"]["checked1"] = "checked";
        } else {
            $arg["data"]["checked2"] = "checked";
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "left_select",
                            "size"        => "6",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move2('right','left_select','right_select',1)\" ",
                            "options"     => $opt_left));

        $objForm->ae( array("type"        => "select",
                            "name"        => "right_select",
                            "size"        => "6",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move2('left','left_select','right_select',1)\" ",
                            "options"     => $opt_right));  
                    
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return move2('sel_add_all','left_select','right_select',1);\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return move2('left','left_select','right_select',1);\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"return move2('right','left_select','right_select',1);\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"return move2('sel_del_all','left_select','right_select',1);\"" ) ); 
                            
        $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("left_select"),
                                   "RIGHT_PART"  => $objForm->ge("right_select"),
                                   "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                                   "SEL_ADD"     => $objForm->ge("sel_add"),
                                   "SEL_DEL"     => $objForm->ge("sel_del"),
                                   "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));

        $arg["info"] = array("LEFT_LIST"  => "費目中分類割当一覧",
                             "RIGHT_LIST" => "費目中分類一覧");

        $objForm->ae( array("type" => "button",
                            "name"        => "btn_add",
                            "value"       => "追 加",
                            "extrahtml"   => "onclick=\"return doSubmit('add1');\"" ) );

        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //更新ボタンを作成
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return doSubmit('update1');\"" ) );

        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //クリアボタンを作成
        $objForm->ae( array("type" => "reset",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit(document.forms[0].cmd.value);\"" ) );

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタンを作成
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"return closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd",
                            "value"     => $model->cmd) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata") );  

        #2005/12/14 
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "inst_cd",
                            "value"     => $model->inst_cd) );


        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit1"){
            $arg["reload"]  = "parent.top_frame.location.href='knjp140kindex.php?cmd=list1';parent.mid_frame.location.href='knjp140kindex.php?cmd=list2'; ";
        }

        View::toHTML($model, "knjp140kForm3.html", $arg);
    }
}
?>
