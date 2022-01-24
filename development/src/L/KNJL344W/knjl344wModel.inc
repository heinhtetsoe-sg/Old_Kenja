<?php

require_once('for_php7.php');

class knjl344wModel extends Model {
    var $data   = array();
    var $cmd;
    var $ObjYear;
    var $schoolcd;  //学校コード

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->ObjYear);
        }

        $this->ObjYear = CTRL_YEAR+1;

        $this->cmd = VARS::request("cmd");

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    function getDownloadModel() {
        $db = Query::dbCheckOut();

        //初期化
        $contents  = "";

        /****************************/
        /*  タイトル部分データ取得  */
        /****************************/
        //年度（和暦）
        $nendo = common::DateConv1($this->ObjYear."/04/01",10);
        //作成日（和暦）
        $date = common::DateConv1(str_replace("-", "/", CTRL_DATE), 0);

        //学校情報
        $school_info = $db->getRow(knjl344wQuery::getSchoolInfo($this), DB_FETCHMODE_ASSOC);
        //課程名
        $courseName = $db->getOne(knjl344wQuery::getCourseName($this));

        /**********************/
        /*  タイトル部分出力  */
        /**********************/
        //1行目
        $contents .= $nendo."　覚書等に基づく高等学校入学志願者の受検・入学等の状況調べ\n";
        //2行目
        $contents .= ",,〔".$date."現在〕\n";
        //3行目
        $contents .= "学区：".$school_info["DIST_NAME"].",,,";
        $contents .= "学校名：".$school_info["FINSCHOOL_NAME"].",,,";
        $contents .= "課程：".$courseName."\n";

        /**********************/
        /*  ヘッダー（一覧）  */
        /**********************/
        $header = array();
        $header["NAME"]             = "生徒名";
        $header["SEX_NAME"]         = "性別";
        $header["FINSCHOOL_NAME"]   = "出身中学校";
        $header["SUC_MAJOR_NAME"]   = "学科名";
        $header["TESTDIV_NAME"]     = "選抜の種類";
        $header["DAI2_SHIBOU"]      = "第２志望";
        $header["JUDGE"]            = "合否";
        $header["ENTDIV"]           = "入学の有無";

        /**********************/
        /*  ヘッダー部分出力  */
        /**********************/
        $header_data = false;   //出力フラグ
        $contents .= implode($header, ",")."\n";

        /********************/
        /*  データ部分出力  */
        /********************/
        $boy = $girl = $total = 0;
        $pass = $fail = $un_exam = $enter = 0;
        $query = knjl344wQuery::selectCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            //ヘッダ順に値をセット
            $sep = "";
            foreach ($header as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";

            //集計
            $total++;
            if ($row["SEX"] == "1") $boy++;
            if ($row["SEX"] == "2") $girl++;
            if ($row["JUDGE"] == "○") $pass++;
            if ($row["JUDGE"] == "×") $fail++;
            if ($row["JUDGE"] == "－") $un_exam++;
            if ($row["ENTDIV"] == "○") $enter++;

            $header_data = true;
        }
        $result->free();

        /**************/
        /*  集計出力  */
        /**************/
        $sumData = array();
        $sumData["NAME"]            = array("合計","男","女");
        $sumData["SEX_NAME"]        = array($total, $boy, $girl);
        $sumData["FINSCHOOL_NAME"]  = array();
        $sumData["SUC_MAJOR_NAME"]  = array("合格数", "不合格数", "不受検数");
        $sumData["TESTDIV_NAME"]    = array($pass, $fail, $un_exam);
        $sumData["DAI2_SHIBOU"]     = array();
        $sumData["JUDGE"]           = array("入学者数", "", "");
        $sumData["ENTDIV"]          = array($enter, "", "");

        for ($i = 0; $i < 3; $i++) {
            $sep = "";
            foreach ($header as $field => $label) {
                $contents .= $sep.$sumData[$field][$i];
                $sep = ",";
            }
            $contents .= "\n";
        }

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            //ファイル名
            $title = $nendo.'　覚書等に基づく高等学校入学志願者の受検・入学等の状況調べ';

            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //県への報告
    function &getUpdateEdboardModel() {
        //県側・一覧テーブル
        //県側・報告テーブル
        knjl344wQuery::getUpdateReport($this);
        $this->print = "on";
        $this->setMessage("MSG201");
    }
}
?>
