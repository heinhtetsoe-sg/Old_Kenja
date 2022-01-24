<?php

require_once('for_php7.php');

class knjl363wModel extends Model {
    var $data   = array();
    var $cmd;
    var $ObjYear;

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->ObjYear);
        }

        $this->getPropertiesAll(); //プロパティファイルの読込み

        $this->ObjYear = CTRL_YEAR+1;

        $this->cmd = VARS::request("cmd");

        $this->selectdata = VARS::post("selectdata");
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    function getDownloadModel() {
        $db = Query::dbCheckOut();

        //初期化
        $contents  = "";
        $header_data = false;   //出力フラグ

        $selectdata = ($this->selectdata) ? explode(',', $this->selectdata) : array();
        foreach ($selectdata as $key => $schoolCd) {

            if ($contents) $contents .= "\n\n";

            /****************************/
            /*  タイトル部分データ取得  */
            /****************************/
            //年度（和暦）
            $nendo = common::DateConv1($this->ObjYear."/04/01",10);
            //作成日（和暦）
            $date = common::DateConv1(str_replace("-", "/", CTRL_DATE), 0);
            //学校名
            $schoolName = $db->getOne(knjl363wQuery::getSchoolName($this, $schoolCd));
            //課程名
            $courseName = $db->getOne(knjl363wQuery::getCourseName($this, $schoolCd));

            /**********************/
            /*  タイトル部分出力  */
            /**********************/
            //1行目
            $contents .= $nendo."三重県立高等学校入学志願者検査個人別・出身中学校別一覧表\n";
            //2行目
            $contents .= ",,〔".$date."現在〕\n";
            //3行目
            $contents .= ",,";
            $contents .= "学校コード：".$schoolCd.",";
            $contents .= "学校名：".$schoolName.",,,";
            $contents .= "課程：".$courseName."\n";
            //4行目
            $contents .= ",,,,,,学力検査等得点,,,,,,,各教科の学習の記録の評定（３年）\n";

            /**********************/
            /*  ヘッダー（一覧）  */
            /**********************/
            $header = array();
            $header["TESTDIV_NAME"]         = "選抜名";
            $header["DAI1_NAME"]            = "学科・コース";
            $header["EXAMNO"]               = "受検番号";
            $header["FINSCHOOL_NAME"]       = "出身中学校";
            $header["SEX_NAME"]             = "男女別";
            $header["SCORE1"]               = "国語";
            $header["SCORE2"]               = "社会";
            $header["SCORE3"]               = "数学";
            $header["SCORE4"]               = "理科";
            $header["SCORE5"]               = "英語";
            $header["SCORE6"]               = "実技";
            $header["SCORET"]               = "小計";
            $header["CONF3_RPT01"]          = "国語";
            $header["CONF3_RPT02"]          = "社会";
            $header["CONF3_RPT03"]          = "数学";
            $header["CONF3_RPT04"]          = "理科";
            $header["CONF3_RPT05"]          = "音楽";
            $header["CONF3_RPT06"]          = "美術";
            $header["CONF3_RPT07"]          = "保健体育";
            $header["CONF3_RPT08"]          = "技術・家庭";
            $header["CONF3_RPT09"]          = "外国語";
            $header["CONF3_TOTAL"]          = "小計";
            $header["CONF3_TOTAL100"]       = "調査書諸記録の100%";
            $header["SCORET80"]             = "学力検査等得点合計の80%";
            $header["JUDGE"]                = "合否";
            $header["DAI2_SUC_MAJOR_NAME"]  = "第２志望合格者（合格学科名）";
            $header["CONF1_TOTAL"]          = "一年の評定合計";
            $header["CONF2_TOTAL"]          = "二年の評定合計";
            $header["REMARK"]               = "備考";
            //各内容をつなげて備考にセット
            $remark = array();
            $remark["YOUSIKI3_REMARK"]      = "別紙様式３の備考";
            $remark["REMARK_SOTUGYOU"]      = "過年度卒業";
            $remark["REMARK_JITAI"]         = "入学辞退";
            $remark["REMARK_TOKU_WAKU"]     = "特別枠選抜";
            $remark["REMARK_OVER_AGE"]      = "成人";
            $remark["REMARK_KESSEKI"]       = "欠席";

            /**********************/
            /*  ヘッダー部分出力  */
            /**********************/
            $contents .= implode($header, ",")."\n";

            /********************/
            /*  データ部分出力  */
            /********************/
            $query = knjl363wQuery::selectCsvQuery($this, $schoolCd);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                //ヘッダ順に値をセット
                $sep = "";
                foreach ($header as $field => $label) {
                    //各内容をつなげて備考にセット
                    if ($field === "REMARK") {
                        $row[$field] = "";
                        $sepRmk = "";
                        foreach ($remark as $fieldRmk => $labelRmk) {
                            //別紙様式３の備考の時
                            if (strlen($row[$fieldRmk]) && $fieldRmk === "YOUSIKI3_REMARK") {
                                $row[$field] .= $sepRmk.$row[$fieldRmk];
                                $sepRmk = "、";
                            } else if ($row[$fieldRmk] === "1") {
                                $row[$field] .= $sepRmk.$labelRmk;
                                $sepRmk = "、";
                            }
                        }
                    }
                    //校外願変
                    if ($row["JUDGEMENT"] === "5") {
                        if ($field != "TESTDIV_NAME" && $field != "DAI1_NAME" && $field != "EXAMNO" && $field != "REMARK") {
                            $row[$field] = "";
                        }
                        if ($field === "REMARK") {
                            $row[$field] = "校外願変";
                        }
                    }
                    $contents .= $sep.$row[$field];
                    $sep = ",";
                }
                $contents .= "\n";

                $header_data = true;
            }
            $result->free();
        }

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            //ファイル名
            $title = $nendo."三重県立高等学校入学志願者検査個人別・出身中学校別一覧表";

            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //報告取り下げ処理
    function &getCancelModel() {
        knjl363wQuery::getDeleteReportFixed($this);
        $this->setMessage("MSG201");
    }
}
?>
