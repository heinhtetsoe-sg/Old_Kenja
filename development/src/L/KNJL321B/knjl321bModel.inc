<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl321bModel extends Model
{
    public $field      = array();
    public $cmd;
    public $ObjYear;
    public $control;   //コントロールマスタ
    public $staffcd;   //職員コード
    public $nameL008 = array();
        
    /**
     * メンバ変数の初期化
     */
    public function knjl321bModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear = CTRL_YEAR+1;
    }
    public function init()
    {
        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),   //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),        //入試区分
                             "SORT"         =>  VARS::post("SORT"),            //表示順
                             "SPECIAL_REASON_DIV" =>  VARS::post("SPECIAL_REASON_DIV"),  //特別措置者のみ
                     );

        $this->cmd         = VARS::request("cmd");

        //DB接続
        $db = Query::dbCheckOut();

        //5教科の取得
        $query = knjl321bQuery::getNameMstL008($this->ObjYear);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $key = $row["NAMECD2"];
            $this->nameL008[$key]["NAMECD2"] = $row["NAMECD2"];
            $this->nameL008[$key]["NAME1"] = $row["NAME1"];
            $this->nameL008[$key]["ABBV1"] = $row["ABBV1"];
            $this->nameL008[$key]["NAMESPARE1"] = $row["NAMESPARE1"];
        }

        //DB切断
        Query::dbCheckIn($db);
    }
    
    //CSV出力処理
    public function getDownloadModel()
    {
        //変数定義
        $comma = ","; //カンマ
        $lf = "\n";   //改行
        //DB接続
        $db = Query::dbCheckOut();

        $query = knjl321bQuery::getNameMstTestSubclass($this);
        $result = $db->query($query);
        $subclassnames = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $subclassnames[$row["TESTSUBCLASSCD"]]= $row["NAME1"];
        }

        //ヘッダ配列
        $header = array();
        $header[] = "合否区分";
        $header[] = "順位";
        $header[] = "受験番号";
        $header[] = "氏名";
        $header[] = "性別";
        $header[] = "出身中略称";

        foreach ($subclassnames as $subclasscd => $subclassname) {
            $header[]= $subclassname;
        }
        $header[] = "3科合計";
        if ($this->field["TESTDIV"] == '1') {
            $header[] = "3科平均";
        }
        if ($this->field["TESTDIV"] == '2') {
            $header[] = "5科合計";
            $header[] = "3科平均";
            $header[] = "5科平均";
            $header[] = "判定得点率";
        }
        $header[] = "当日チェック";
        foreach ($this->nameL008 as $key => $record) {
            $header[] = $record["ABBV1"]."評定"; //内申点 教科名
        }
        $header[] = "5教科平均";
        $header[] = "評定平均";
        $header[] = "評価1";
        $header[] = "行動";
        $header[] = "基本○無";
        $header[] = "中1欠";
        $header[] = "中2欠";
        $header[] = "中3欠";
        $header[] = "欠席理由";
        if ($this->field["TESTDIV"] == '1') {
            $header[] = "入試相談点1";
            $header[] = "入試相談点2";
            $header[] = "確約区分";
        }
        $header[] = "クラブ名";
        if ($this->field["TESTDIV"] == '1') {
            $header[] = "推薦理由";
        }
        $header[] = "調査書備考";


        //ヘッダ配列を出力変数にセット
        $headerContents = implode($header, $comma) .$lf;

        //CSVデータを取得し出力変数にセット
        $data = false;
        $dataContents = "";
        $query = knjl321bQuery::getCsvQuery($this);
        $result = $db->query($query);
        $oldExamno = "";
        $applicants = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($oldExamno == "" || $oldExamno != $row["EXAMNO"]) {
                $examno = $row["EXAMNO"];
                $applicants[$examno]["EXAMNO"] = $row["EXAMNO"];
                $applicants[$examno]["JUDGEDIV_NAME"] = $row["JUDGEDIV_NAME"];
                $applicants[$examno]["TOTAL_RANK4"] = $row["TOTAL_RANK4"];
                $applicants[$examno]["SELECT_SUBCLASS_DIV_ABBV2"] = $row["SELECT_SUBCLASS_DIV_ABBV2"];
                $applicants[$examno]["NAME"] = $row["NAME"];
                $applicants[$examno]["SEX_NAME"] = $row["SEX_NAME"];
                $applicants[$examno]["FINSCHOOL_NAME_ABBV"] = $row["FINSCHOOL_NAME_ABBV"];
                $applicants[$examno]["TOTAL3"] = $row["TOTAL3"];
                $applicants[$examno]["TOTAL1"] = $row["TOTAL1"];
                $applicants[$examno]["AVARAGE3"] = $row["AVARAGE3"];
                $applicants[$examno]["AVARAGE1"] = $row["AVARAGE1"];
                $applicants[$examno]["AVARAGE4"] = $row["AVARAGE4"];
                $applicants[$examno]["INTERVIEW_A"] = $row["INTERVIEW_A"];
                $applicants[$examno]["INTERVIEW_B"] = $row["INTERVIEW_B"];
                $applicants[$examno]["INTERVIEW_C"] = $row["INTERVIEW_C"];
                $applicants[$examno]["REMARK1"] = $row["REMARK1"];

                //内申点
                $aveDiv = ""; //5教科平均
                $totalDiv = 0;
                $divNum = 1;
                foreach ($this->nameL008 as $key => $record) {
                    if ($record["NAMESPARE1"] == "1") {
                        $field = "CONFIDENTIAL_RPT".$key;
                        $div = ($row[$field]) ? $row[$field] : 0;
                        $totalDiv = $totalDiv + $div; //NAMESPARE1 = '1' の教科を加算
                        $applicants[$examno]["DIV".$divNum] = $div; //内申点 教科1 ～ 教科5
                        $divNum++;
                    }
                }
                if ($totalDiv != 0) {
                    $aveDiv = $totalDiv / 5; //5教科平均
                    $aveDiv = round($aveDiv, 1); //四捨五入
                }
                $applicants[$examno]["AVE_DIV"] = $aveDiv;

                $applicants[$examno]["HAS_CONFRPT_DAT"] = $row["HAS_CONFRPT_DAT"];
                $applicants[$examno]["AVERAGE_ALL"] = $row["AVERAGE_ALL"];
                $confidentialRptHas1 = "";
                for ($i = 1; $i <= 9; $i++) {
                    $confRptn = $row["CONFIDENTIAL_RPT0".$i];
                    if ("1" == $confRptn) {
                        $confidentialRptHas1 = "1";
                    }
                }
                $applicants[$examno]["CONFIDENTIAL_RPT_HAS1"] = $confidentialRptHas1;
                $flgMaruCount = 0;
                for ($i = 1; $i <= 10; $i++) {
                    $flgn = $row["F".$i];
                    if ("1" == $flgn) {
                        $flgMaruCount += 1;
                    }
                }
                $applicants[$examno]["FLG_MARU_COUNT"] = $flgMaruCount < 2 ? "1" : "";
                $applicants[$examno]["BASE_FLG_SHOW"] = $row["BASE_FLG_SHOW"];
                $applicants[$examno]["ABSENCE_DAYS"] = $row["ABSENCE_DAYS"];
                $applicants[$examno]["ABSENCE_DAYS2"] = $row["ABSENCE_DAYS2"];
                $applicants[$examno]["ABSENCE_DAYS3"] = $row["ABSENCE_DAYS3"];
                $absenceRemarkAll = "";
                $c = "";
                for ($i = 1; $i <= 3; $i++) {
                    $rem = $row["ABSENCE_REMARK".($i == 1 ? "" : $i)];
                    if ($rem) {
                        $absenceRemarkAll .= $c.$rem;
                        $c = "、";
                    }
                }
                $applicants[$examno]["ABSENCE_REMARK_ALL"] = $absenceRemarkAll;
                $applicants[$examno]["BASE_DETAIL4_REMARK3"] = $row["BASE_DETAIL4_REMARK3"];
                $applicants[$examno]["BASE_DETAIL4_REMARK4"] = $row["BASE_DETAIL4_REMARK4"];
                $applicants[$examno]["PROMISE_COURSE_ABBV"] = $row["PROMISE_COURSE_ABBV"];
                $applicants[$examno]["BASE_DETAIL4_REMARK1"] = $row["BASE_DETAIL4_REMARK1"];
                $applicants[$examno]["BASE_DETAIL4_REMARK2"] = $row["BASE_DETAIL4_REMARK2"];
                $applicants[$examno]["CONFRPT_REMARK1"] = $row["CONFRPT_REMARK1"];
            }
            $applicants[$examno]["TESTSUBCLASSCD".$row["TESTSUBCLASSCD"]] = $row["SCORE"];
        }

        foreach ($applicants as $examno => $applicant) {
            //置換
            $applicant = str_replace("\"", "\"\"", $applicant);
            $applicant = str_replace("\r\n", "", $applicant);
            $applicant = str_replace("\r", "", $applicant);
            $applicant = str_replace("\n", "", $applicant);
            
            $dataContents .= '"'.$applicant["JUDGEDIV_NAME"].'"'.$comma;
            $dataContents .= '"'.$applicant["TOTAL_RANK4"].'"'.$comma;
            $dataContents .= '"'.$applicant["EXAMNO"].'"'.$comma;
            $dataContents .= '"'.$applicant["NAME"].'"'.$comma;
            $dataContents .= '"'.$applicant["SEX_NAME"].'"'.$comma;
            $dataContents .= '"'.$applicant["FINSCHOOL_NAME_ABBV"].'"'.$comma;
            foreach ($subclassnames as $subclasscd => $subclassname) {
                $dataContents .= '"'.$applicant["TESTSUBCLASSCD".$subclasscd].'"'.$comma;
            }
            $dataContents .= '"'.$applicant["TOTAL3"].'"'.$comma;
            if ("1" == $this->field["TESTDIV"]) {
                $dataContents .= '"'.$applicant["AVARAGE3"].'"'.$comma;
            }
            if ("2" == $this->field["TESTDIV"]) {
                $dataContents .= '"'.$applicant["TOTAL1"].'"'.$comma;
                $dataContents .= '"'.$applicant["AVARAGE3"].'"'.$comma;
                $dataContents .= '"'.$applicant["AVARAGE1"].'"'.$comma;
                $dataContents .= '"'.$applicant["AVARAGE4"].'"'.$comma;
            }
            $dataContents .= '"'.$applicant["REMARK1"].'"'.$comma;

            //内申点
            $dataContents .= '"'.$applicant["DIV1"].'"'.$comma;
            $dataContents .= '"'.$applicant["DIV2"].'"'.$comma;
            $dataContents .= '"'.$applicant["DIV3"].'"'.$comma;
            $dataContents .= '"'.$applicant["DIV4"].'"'.$comma;
            $dataContents .= '"'.$applicant["DIV5"].'"'.$comma;
            $dataContents .= '"'.$applicant["AVE_DIV"].'"'.$comma;

            $dataContents .= '"'.$applicant["AVERAGE_ALL"].'"'.$comma;
            $dataContents .= '"'.$applicant["CONFIDENTIAL_RPT_HAS1"].'"'.$comma;
            $dataContents .= ($applicant["HAS_CONFRPT_DAT"] ? '"'.$applicant["FLG_MARU_COUNT"].'"' : '"'."".'"').$comma;
            $dataContents .= ($applicant["HAS_CONFRPT_DAT"] ? '"'.$applicant["BASE_FLG_SHOW"].'"' : '"'."".'"').$comma;
            $dataContents .= '"'.$applicant["ABSENCE_DAYS"].'"'.$comma;
            $dataContents .= '"'.$applicant["ABSENCE_DAYS2"].'"'.$comma;
            $dataContents .= '"'.$applicant["ABSENCE_DAYS3"].'"'.$comma;
            $dataContents .= '"'.$applicant["ABSENCE_REMARK_ALL"].'"'.$comma;
            if ("1" == $this->field["TESTDIV"]) {
                $dataContents .= '"'.$applicant["BASE_DETAIL4_REMARK3"].'"'.$comma;
                $dataContents .= '"'.$applicant["BASE_DETAIL4_REMARK4"].'"'.$comma;
                $dataContents .= '"'.$applicant["PROMISE_COURSE_ABBV"].'"'.$comma;
            }
            $dataContents .= '"'.$applicant["BASE_DETAIL4_REMARK1"].'"'.$comma;
            if ("1" == $this->field["TESTDIV"]) {
                $dataContents .= '"'.$applicant["BASE_DETAIL4_REMARK2"].'"'.$comma;
            }
            $dataContents .= '"'.$applicant["CONFRPT_REMARK1"].'"';

            $dataContents .= $lf;
            $data = true;
        }

        //DB切断
        Query::dbCheckIn($db);
        //CSV出力
        //タイトル
        $date = common::DateConv1(str_replace("-", "/", CTRL_DATE), 0);
        $time = "　".date('H時i分現在');
        $title = $this->ObjYear.'年度　合否判定資料（'.("2" == $this->field["SORT"] ? "成績順" : "受験番号順").'）,,,,'.$date.$time.$lf;
        //サブタイトル
        $subtitle = "";
        //$subtitle  =  $comma.$comma.$comma.$comma.$comma."";
        //$subtitle .=  $comma.$comma.$comma.$this->wareki_date.$lf;
        
        $contents  = "";
        $contents .= $title;
        $contents .= $subtitle;
        $contents .= $headerContents;
        $contents .= $dataContents;
        if ($data) {
            $filename = sprintf("%s.csv", "合否判定資料");
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
    
    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}
