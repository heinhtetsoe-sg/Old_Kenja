<?php

require_once('for_php7.php');

class knjp706bForm1
{
    function main(&$model)
    {
        $objForm = new form;

        $db = Query::dbCheckOut();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjp706bindex.php", "", "edit");

        //生徒の名前を取得
        $row = $db->getRow(knjp706bQuery::getStudentName($model),DB_FETCHMODE_ASSOC);
        $arg["NAME"] = $row["SCHREGNO"]."　".$row["NAME"];
        $risyuuCnt = $db->getOne(knjp706bQuery::getRisyuuCnt($model));
        $zanMusyouKaisu = $row["MUSYOU_KAISU"] < $risyuuCnt ? 0 : $row["MUSYOU_KAISU"] - $risyuuCnt;
        $arg["MUSYOU_KAISU"] = $zanMusyouKaisu;

        //リスト取得
        $result = $db->query(knjp706bQuery::getList($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");
            //リンク設定
            $row["COLLECT_M_CD"] = View::alink("knjp706bindex.php", $row["COLLECT_GRP_CD"].":".$row["COLLECT_L_CD"].$row["COLLECT_M_CD"], "target=bottom_frame",
                                    array("COLLECT_GRP_CD"      => $row["COLLECT_GRP_CD"],
                                          "COLLECT_M_CD"        => $row["COLLECT_M_CD"],
                                          "COLLECT_L_CD"        => $row["COLLECT_L_CD"],
                                          "COLLECT_M_NAME"      => $row["COLLECT_M_NAME"],
                                          "MONEY_DUE"           => $row["MONEY_DUE"],
                                          "COLLECT_S_EXIST_FLG" => $row["COLLECT_S_EXIST_FLG"],
                                          "PAY_DIV"             => $row["PAY_DIV"],
                                          "INST_CD"             => $row["INST_CD"],
                                          "cmd"                 => "edit"));

            //金額フォーマット
            $row["MONEY_DUE"] = (strlen($row["MONEY_DUE"])) ? number_format($row["MONEY_DUE"]): "";
            $row["PAID_MONEY"] = (strlen($row["PAID_MONEY"])) ? number_format($row["PAID_MONEY"]): "";
            $row["REPAY_MONEY"] = (strlen($row["REPAY_MONEY"])) ? number_format($row["REPAY_MONEY"]): "";

            //入金区分
            if (strlen($row["PAID_MONEY_DIV"])) {
                $row["PAID_MONEY_DIV"] = $db->getOne(knjp706bQuery::getName($model->year, "G205", $row["PAID_MONEY_DIV"]));
            }

            //振替停止
            if (strlen($row["BANK_TRANS_STOP_RESON"])) {
                $row["BANK_TRANS_STOP_RESON"] = "停止";
            }

            //分納
            if (strlen($row["INST_CD"])) {
                $row["INST_CD"] = "分納";
            }

            //軽減 NO003
            if ($row["REDUC_DEC_FLG_1"] == 1 && $row["REDUC_DEC_FLG_2"] == 1) {
                $row["REDUC_DEC_FLG"] = "有";
            }else {
                $row["REDUC_DEC_FLG"] = "";
            }

            //日付変換
            $row["PAID_MONEY_DATE"] = str_replace("-", "/", $row["PAID_MONEY_DATE"]);

            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);   

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjp706bForm1.html", $arg);
    }
}
?>
