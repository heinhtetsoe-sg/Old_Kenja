<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjp331Model extends Model {
    var $field      = array();
    var $output;
    var $todoufuken;
    var $hrclass;
    var $selectdata = array();
    var $cmd;

    /**
     * メンバ変数の初期化
     */
    function knjp331Model()
    {
    }

    function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "DATE1"            =>  VARS::post("DATE1"),            //日付FROM
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->output     = VARS::post("OUTPUT");
        $this->todoufuken = VARS::post("TODOUFUKEN");
        $this->hrclass    = VARS::post("HRCLASS");
        $this->cmd        = VARS::request("cmd");
        $this->selectdata = VARS::post("selectdata");
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }

    /**
     * CSV出力
     */
    function getDownloadModel()
    {
        $data = false;

        $db = Query::dbCheckOut();

        $inState = "('".str_replace(",", "','", VARS::post("selectdata"))."')";
        //SQL文発行
        if ($this->output == "1") {
            $query = knjp331Query::classMeisai($this, $inState);
        } else {
            $query = knjp331Query::kojinMeisai($this, $inState);
        }

        $result = $db->query($query);

        list($date1Y, $date1M, $date1D) = preg_split("/\//", $this->field["DATE1"]);
        $nendo1Y = $date1M < "04" ? $date1Y - 1 : $date1Y;
        $date1WarekiNendo = common::Calc_Wareki($nendo1Y, $date1M, $date1D);
        $date1WarekiNen = common::Calc_Wareki($date1Y, $date1M, $date1D);
        $date1WarekiDate = $date1WarekiNen.$date1M."月".$date1D."日";

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            $contents .= ",就学支援金等給付決定通知書";
            $contents .= "\n\n";
            $contents .= ",,,,,".$date1WarekiDate;
            $contents .= "\n\n";
            $contents .= "申請者（学資負担者）";
            $contents .= "\n\n";
            $contents .= $row["GUARANTOR_NAME"]."様";
            $contents .= "\n\n";
            $contents .= "（".$row["HR_NAME"]." ".$row["ATTENDNO"]."番  ".$row["NAME"]." ）\n\n";
            $contents .= "\n\n\n";
            $contents .= ",,,,学校法人,近　畿　大　学";
            $contents .= "\n\n\n";
            $contents .= ",,,,理 事 長,世　耕　弘　成";
            $contents .= "\n\n\n\n";
            $contents .= ",　このたび国より". $date1WarekiNendo ."度高等学校等就学支援金の交付を受け";
            $contents .= "\n\n";
            $contents .= ",ることになりましたので、下記のとおり給付いたします。";
            $contents .= "\n\n\n\n";
            $contents .= ",,,記";
            $contents .= "\n\n\n\n";
            $reductionmoney = number_format($row["TOTAL"]);
            $contents .= ",,1 . 決定給付額,\"". $reductionmoney ."円\"";
            $contents .= "\n\n\n";
            $contents .= ",,2 . 給付方法,3月中に授業料登録口座に振込";
            $contents .= "\n\n\n";
            $baseMoney = number_format($row["PAID_MONEY"]);
            $setBmin = substr($row["B_MIN"], 4);
            $setBmax = substr($row["B_MAX"], 4);
            $contents .= ",,,就学支援金(国)　基本({$setBmin}-{$setBmax}月分),①,\"". $baseMoney ."円\"";
            $contents .= "\n\n";
            $addMoney = number_format($row["ADD_PAID_MONEY"]);
            $setAmin = substr($row["A_MIN"], 4);
            $setAmax = substr($row["A_MAX"], 4);
            $contents .= ",,,就学支援金(国)　加算({$setAmin}-{$setAmax}月分),②,\"". $addMoney ."円\"";
            $contents .= "\n\n";
            if ($row["OVER_MONEY"] > 0) {
                $overMoney = number_format($row["OVER_MONEY"]);
                $contents .= ",,,就学支援奨学金(学校負担金),③,\"". $overMoney ."円\"";
            }
            $contents .= "\n\n";
            if ($row["OVER_MONEY"] > 0) {
                $contents .= ",,,振込額,(①+②+③),\"". $reductionmoney ."円\"";
            } else {
                $contents .= ",,,振込額,(①+②),\"". $reductionmoney ."円\"";
            }
            $contents .= "\n\n";

            $contents .= "\n\n\n\n\n\n\n\n\n";
            $data = true;
        }
        $result->free();

        Query::dbCheckIn($db);

        if ($data) {
            $filename = CTRL_YEAR."年度_就学支援金等給付決定通知書.csv";
            //ファイルダウンロード
            common::downloadContents($contents, $filename);

            return true;
        }else{
            $this->setWarning("MSG303");    //データは存在していません。
            return false;
        }
    }
}
?>
