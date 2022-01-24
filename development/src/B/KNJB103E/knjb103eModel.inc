<?php

require_once('for_php7.php');

class knjb103eModel extends Model
{
    public $field      = array();
    public $cmd;

    public function knjb103eModel()
    {
    }

    public function init()
    {
        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "TERGET_DATE"          =>  VARS::post("TERGET_DATE"),          //対象日付
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    public function clean()
    {
    }

    public function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //指定週の開始日(月曜日)と終了日(金曜日)を取得
        $this->field["FROM_DATE"] = $this->getFromDate($this->field["TERGET_DATE"]);
        $this->field["TO_DATE"] = $this->getToDate($this->field["TERGET_DATE"]);

        //曜日コード（SQLのDAYOFWEEK）
        $weekList = array();
        $weekList[] = array("DAYCD" => "2", "DAYNAME" => "月");
        $weekList[] = array("DAYCD" => "3", "DAYNAME" => "火");
        $weekList[] = array("DAYCD" => "4", "DAYNAME" => "水");
        $weekList[] = array("DAYCD" => "5", "DAYNAME" => "木");
        $weekList[] = array("DAYCD" => "6", "DAYNAME" => "金");

        //ヘッダ
        $header = array();
        $header["HR_NAMEABBV"]          = "ＨＲ";
        $header["STAFFNAME"]            = "氏名";
        $header["SECTIONABBV"]          = "所属";
        //ヘッダ（時間割）
        foreach ($weekList as $weekKey => $weekArray) {
            $periodCnt = 1;
            for ($period = 2; $period <= 8; $period++) {
                $celKey = $weekArray["DAYCD"]."-".$period;
                $header["JOUDAN_GEDAN".$celKey] = $weekArray["DAYNAME"].$periodCnt;
                $periodCnt++;
            }
        }
        $header["KOUZA_TOTAL"]          = "時間数";
        $header["STAFFNAME_END"]        = "氏名";

        //ヘッダ出力
        $header_data = false;   //出力フラグ
        $contents  = "";        //クリア
        $contents .= implode($header, ",") ."\n";

        //CSV出力
        $csv = array();
        $query = knjb103eQuery::getCsvList($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",", ".", $row);
            $row = str_replace("\r", "", $row);
            $row = str_replace("\n", "", $row);

            //同一時限に複数講座が存在する場合は講座コード順に表示するために行番号（LINE_NO）を割り振る
            $lineKey = $row["GRADE"]."-".$row["HR_CLASS"]."-".$row["STAFFCD"]."-".$row["LINE_NO"];
            $celKey = $row["DAYCD"]."-".$row["PERIODCD"];

            $row["JOUDAN"] = $row["CHAIRNAME"].$row["SMALLCLS"];
            $row["GEDAN"] = $row["FACILITYABBV"]."(".sprintf("%d", $row["TRGTGRADE"]).")";

            $csv[$lineKey]["HR_NAMEABBV"] = $row["HR_NAMEABBV"];
            $csv[$lineKey]["STAFFNAME"] = $row["STAFFNAME"];
            $csv[$lineKey]["SECTIONABBV"] = $row["SECTIONABBV"];
            $csv[$lineKey]["KOUZA_TOTAL"] = $row["KOUZA_TOTAL"];
            $csv[$lineKey]["STAFFNAME_END"] = $row["STAFFNAME"];
            $csv[$lineKey]["JOUDAN_GEDAN".$celKey] = $row["JOUDAN"]."/".$row["GEDAN"];
        }
        foreach ($csv as $lineKey => $row) {
            $sep = "";
            foreach ($header as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }

            $contents .= "\n";
            $header_data = true;
        }

        //ファイル名
        $termDate = $this->getDateMDW($this->field["FROM_DATE"])."　".$this->getDateMDW($this->field["TO_DATE"]);
        $nendo = knjb103eQuery::getAutoFormatYear($db, CTRL_YEAR);
        $title = $nendo."度　時間割　".$termDate;

        //DB切断
        Query::dbCheckIn($db);

        if ($header_data) {
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "出力データはありません。";
            return false;
        }
    }

    public function getDateMDW($date)
    {
        $wday = array("日","月","火","水","木","金","土");
        $rtn = "";
        if (preg_match("/([0-9]{4})\/([0-9]{1,2})\/([0-9]{1,2})/", $date, $regs)) {
            $year   = (int)$regs[1];
            $month  = (int)$regs[2];
            $day    = (int)$regs[3];
            $w = Date_Calc::dateFormat($day, $month, $year, "%w");
            $rtn = sprintf("%d月%d日(%s)", $month, $day, $wday[$w]);
        }
        return $rtn;
    }

    public function getFromDate($tergetDate)
    {
        $rtnDate = "";
        if (preg_match("/([0-9]{4})\/([0-9]{1,2})\/([0-9]{1,2})/", $tergetDate, $regs)) {
            $year   = (int)$regs[1];
            $month  = (int)$regs[2];
            $day    = (int)$regs[3];
            $w = Date_Calc::dateFormat($day, $month, $year, "%w");
            $rtnDate = Date_Calc::daysToDate(Date_Calc::dateToDays($day+1-$w, $month, $year), "%Y/%m/%d");

            //指定週が年度をまたぐ場合
            $sDate = CTRL_YEAR . "/04/01";
            $eDate = (CTRL_YEAR + 1) . "/03/31";
            if ($rtnDate < $sDate || $rtnDate > $eDate) {
                $rtnDate = ($rtnDate < $sDate) ? $sDate : $eDate;
            }
        }
        return $rtnDate;
    }

    public function getToDate($tergetDate)
    {
        $rtnDate = "";
        if (preg_match("/([0-9]{4})\/([0-9]{1,2})\/([0-9]{1,2})/", $tergetDate, $regs)) {
            $year   = (int)$regs[1];
            $month  = (int)$regs[2];
            $day    = (int)$regs[3];
            $w = Date_Calc::dateFormat($day, $month, $year, "%w");
            $rtnDate = Date_Calc::daysToDate(Date_Calc::dateToDays($day+5-$w, $month, $year), "%Y/%m/%d");

            //指定週が年度をまたぐ場合
            $sDate = CTRL_YEAR . "/04/01";
            $eDate = (CTRL_YEAR + 1) . "/03/31";
            if ($rtnDate < $sDate || $rtnDate > $eDate) {
                $rtnDate = ($rtnDate < $sDate) ? $sDate : $eDate;
            }
        }
        return $rtnDate;
    }
}
