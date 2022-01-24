<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knji080aModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knji080aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("GRADUATE_YEAR"    =>  VARS::post("GRADUATE_YEAR")
                            );

        $this->cmd = VARS::request("cmd");

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    function getDownloadModel()
    {
        //CSVデータ抽出処理
        //DB接続
        $db = Query::dbCheckOut();

        $contents = "";    //CSVデータ出力用
        $data = false;

        //出力データの作成//
        $grdData = array();
        $query = knji080aQuery::selectCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",",".",$row);

            $dataTemp = array();
            $dataTemp["SCHREGNO"]   = $row["SCHREGNO"];
            $dataTemp["NAME"]       = $row["NAME"];
            $dataTemp["SEX"]        = $row["SEX"];
            $dataTemp["GRD_DATE"]   = ($row["GRD_DATE"]) ? str_replace("-","/",$row["GRD_DATE"]) : "";
            $dataTemp["ENT_DATE"]   = ($row["ENT_DATE"]) ? str_replace("-","/",$row["ENT_DATE"]) : "";
            $dataTemp["REMARK"]     = "";

            $grdData[] = $dataTemp;
            $data = true;
        }
        $result->free();

        //タイトル行
        $wrk_nendo = common::DateConv1($this->field["GRADUATE_YEAR"]."/04/01", 10);
        $title = "卒業生一覧";
        $wrk_date = str_replace("-","/",CTRL_DATE);

        $contents  = "{$wrk_nendo}　{$title},,,,,{$wrk_date}\n";
        $setCnt = get_count($grdData);
        $contents .= ",,,,,卒業生：{$setCnt}人\n";

        $header["SCHREGNO"] = "学籍番号";
        $header["NAME"]     = "氏名";
        $header["SEX"]      = "性別";
        $header["GRD_DATE"] = "卒業日";
        $header["ENT_DATE"] = "入学日";
        $header["REMARK"]   = "備考";
        $contents .= implode($header, ",") ."\n";

        foreach ($grdData as $grdKey => $grdVal) {
            $contents .= implode($grdVal, ",") ."\n";
        }

        //DB切断
        Query::dbCheckIn($db);

        if ($data) {
            $filename = sprintf("%s.csv", "卒業生一覧");
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }


    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
