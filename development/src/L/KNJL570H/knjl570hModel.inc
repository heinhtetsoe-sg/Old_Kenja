<?php

require_once('for_php7.php');

class knjl570hModel extends Model {
    var $field  = array();
    var $data   = array();
    var $cmd;
    var $file;
    var $year;

    function init() {
        $this->year = VARS::post("YEAR");

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //学校種別
                             "DISTINCT_ID"      =>  VARS::post("DISTINCT_ID"),      //入試判別
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "HEADER"           =>  VARS::post("HEADER")            //ヘッダ有
                             );

        $this->cmd = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    function getDownloadModel() {
        $db = Query::dbCheckOut();

        //エラー項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        /**********************/
        /*  ヘッダー部分出力  */
        /**********************/
        $header = array();
        $header["EXAMNO"]    = "受験番号";
        $header["NAME"]      = "氏名";
        $header["SEX"]       = "性別";

        //科目（内申）
        $query = knjl570hQuery::getNameMstAll($this->year, "L008");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $name = ($this->field["APPLICANTDIV"] == "2") ? $row["NAME1"]: $row["NAME2"];
            if ($name == "") continue;
            $header["CONFIDENTIAL_RPT".$row["NAMECD2"]] = $name."内申点";
        }
        if ($this->field["APPLICANTDIV"] == "1") { // 中学
            $header["DET001_6"]  = "４年合計";
            $header["DET001_7"]  = "５年合計";
            $header["DET001_8"]  = "６年合計";
        }
        $header["TOTAL_ALL"] = "合計";
        $header["DET001_2"]  = "行動内申点";
        $header["DET001_3"]  = "特別内申点";
        $header["DET001_4"]  = "出席内申点";
        $header["DET001_5"]  = "合計（行動、特別、出席）";

        //科目（得点）
        $this->subClassArr = array();
        $query = knjl570hQuery::getExamTypeTestSubclass($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $header["SCORE_".$row["VALUE"]] = "得点（".$row["LABEL"]."）";
            $this->subClassArr[$row["VALUE"]] = $row["LABEL"];
        }

        $header["FINSCHOOL_ADDR"]   = "出身学校所在地";
        $header["FINSCHOOL_NAME"]   = "出身学校名称";
        $header["PRISCHOOL_NAME"]   = "塾名";
        $header["SH_FLG"]           = "併願の有無";
        $header["XXX"]              = "特記事項";

        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ

        if($this->field["HEADER"] == "on") $contents .= implode($header, ",")."\n";

        /********************/
        /*  データ部分出力  */
        /********************/
        //データ
        $query = knjl570hQuery::selectDataQuery($this);

        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(array("\r\n", "\r", "\n"),"",str_replace(",",".",$row));
            $setArray = $header;

            //ヘッダ順に値をセット
            $sep = "";
            foreach ($setArray as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";

            $header_data = true;
        }
        $result->free();

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $couse = "選考会資料";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
?>
