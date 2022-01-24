<?php

require_once('for_php7.php');

class knjp982Form1
{
    public function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjp982index.php", "", "edit");

        $db = Query::dbCheckOut();

        //年度設定
        $arg["year"] = $model->year;

        //校種コンボ
        $arg["schkind"] = "1";
        $query = knjp982Query::getSchkind($model);
        $extra = "onchange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, $model->schoolKind, "SCHOOL_KIND", $extra, 1, "");

        //給付対象使用するか
        if ($model->Properties["useBenefit"] == "1") {
            $arg["useBenefit"] = "1";
        } else {
            $arg["unUseBenefit"] = "1";
        }

        //SIGELシステム使用するか
        if ($model->Properties["useSIGELsystem"] == "1") {
            $arg["useSIGELsystem"] = 1;
        } else {
            $arg["useSIGELsystem"] = '';
        }

        //リスト表示
        $bifKey = "";
        $result = $db->query(knjp982Query::selectQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            if ($bifKey !== $row["LEVY_L_M_CD"]) {
                $cnt = $db->getOne(knjp982Query::getLevyLMCnt($row["LEVY_L_M_CD"], $model));
                $row["ROWSPAN"] = $cnt > 0 ? $cnt : 1;
            }
            $bifKey = $row["LEVY_L_M_CD"];

            $row["LEVY_S_CD"] = View::alink(
                "knjp982index.php",
                $row["LEVY_S_CD"],
                "target=\"right_frame\"",
                array("cmd"           => "edit",
                                                        "SCHOOL_KIND"   => $model->schoolKind,
                                                        "LEVY_L_M_CD"   => $row["LEVY_L_M_CD"],
                                                        "LEVY_S_CD"     => $row["LEVY_S_CD"] )
            );
            //返金可・不可
            if ($row["REPAY_DIV"] == "1") {
                $row["REPAY_DIV"] = "可";
            } else {
                $row["REPAY_DIV"] = "不可";
            }

            //給付対象
            if ($row["BENEFIT"] == "1") {
                $row["BENEFIT"] = "レ";
            }

            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        //hidden
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "cmd" ));

        if (!isset($model->warning) && $model->cmd == "change") {
            $arg["reload"] = "parent.right_frame.location.href='knjp982index.php?cmd=edit"
                           . "&year=".$model->year."&SCHOOL_KIND=".$model->schoolKind."';";
        }

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp982Form1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
    }
    if ($name == "SCHOOL_KIND" && SCHOOLKIND) {
        $value = ($value && $value_flg) ? $value : SCHOOLKIND;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
