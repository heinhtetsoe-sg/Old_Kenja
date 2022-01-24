<?php

require_once('for_php7.php');

//定数定義

class knjm502mModel extends Model {
    var $cmd;
    var $control;   //コントロールマスタ
    var $selectdata;

    //メンバ変数の初期化
    function knjm502mModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }
    function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->selectdata = VARS::post("selectdata");
    }

    function getDownloadModel() {
        //変数定義
        $comma = ","; //カンマ
        $lf = "\n";   //改行
        //DB接続
        $db = Query::dbCheckOut();

        $arr = explode(",", $this->selectdata);
        $contents  = "";
        foreach ($arr as $subclassCd) {
            $standard_date = array();
            $query = knjm502mQuery::getSubcalssName($subclassCd);
            $subclassName = $db->getOne($query);
            $query = knjm502mQuery::getKiteiCount($subclassCd);
            $kitei = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $contents .= "科目別スクーリング、レポート状況\n";
            $contents .= "\n";
            $contents .= "科目コード,{$subclassCd},,,規定回数,スクーリング {$kitei["SCH_SEQ_MIN"]},レポート {$kitei["REP_SEQ_ALL"]}\n";
            $contents .= "科目名,{$subclassName},,,作成日,".str_replace("-", "/", CTRL_DATE).",\n";
            $contents .= "\n";
            $contents .= "\n";
            $contents .= ",,,,,提出基準日";
            $query = knjm502mQuery::getStandardDate($subclassCd, $this);
            $result = $db->query($query);
            while ($rowDate = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                list($stanYear, $stanMonth, $stanDay) = preg_split("/-/", $rowDate["STANDARD_DATE"]);
                $setStandard = $stanYear."/".$stanMonth."/".$stanDay;
                $contents .= ",,{$setStandard}";
                $standard_date[$rowDate["STANDARD_SEQ"]] = $rowDate["STANDARD_DATE"];
            }
            $result->free();
            $contents .= "\n";
            $contents .= "学籍番号,氏名,卒業予定,,回数,種類";
            for ($k = 1; $k < 25; $k++) {
                $contents .= ",,{$k}回";
            }
            $contents .= "\n";
            $query = knjm502mQuery::getStudent($subclassCd, $this);
            $result = $db->query($query);
            while ($rowStudent = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

                $contents .= $rowStudent["SCHREGNO"].",{$rowStudent["NAME"]},{$rowStudent["BASE_REMARK1"]},S,";
                $query = knjm502mQuery::getSchooling($this, $rowStudent["SCHREGNO"], $rowStudent["CHAIRCD"]);
                $resultSchooling = $db->query($query);

                $kaisuuKei = 0;
                $stuContents = "";
                $stuContents .= ",登校";
                $defSchoolingkindcd = "1";
                $housuoFlg = false;
                while ($rowSchooling = $resultSchooling->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($defSchoolingkindcd != "" && $defSchoolingkindcd != $rowSchooling["SCHOOLINGKINDCD"]) {
                        $stuContents = $kaisuuKei.$stuContents."\n";
                        $stuContents .= ",,,,放送置換,放送";
                        $kaisuuKei = 0;
                        $housuoFlg = true;
                    }
                    $kaisuuKei = $kaisuuKei + $rowSchooling["CREDIT_TIME"];
                    $stuContents .= ",,{$rowSchooling["E_DATE"]}";
                    $defSchoolingkindcd = $rowSchooling["SCHOOLINGKINDCD"];
                }
                $resultSchooling->free();
                if ($housuoFlg) {
                    $stuContents = str_replace("放送置換", $kaisuuKei, $stuContents)."\n";
                } else {
                    $stuContents = $kaisuuKei.$stuContents."\n";
                    $stuContents .= ",,,,0,放送\n";
                }
                $contents .= $stuContents;

                $query = knjm502mQuery::getReport($this, $rowStudent["SCHREGNO"], $rowStudent["CHAIRCD"], $subclassCd);
                $resultReport = $db->query($query);

                $kaisuuKei = 0;
                $stuHyouka  = ",,,R,評価置換,評価";
                $stuUketuke = ",,,,,受付日";
                $passCnt = 0;
                while ($rowReport = $resultReport->fetchRow(DB_FETCHMODE_ASSOC)) {

                    if ($standard_date[$rowReport["STANDARD_SEQ"]] < CTRL_DATE && !$rowReport["HYOUKA"]) {
                        $rowReport["HYOUKA"] = "×";
                    }
                    $stuHyouka  .= ",{$rowReport["REPRESENT_SEQ"]},{$rowReport["HYOUKA"]}";

                    list($repYear, $repMonth, $repDay) = preg_split("/-/", $rowReport["RECEIPT_DATE"]);
                    $setRepDate = ($rowReport["RECEIPT_DATE"]) ? $repYear."/".$repMonth."/".$repDay : "";
                    $stuUketuke .= ",,{$setRepDate}";
                    $passCnt = ($rowReport["PASS"] == "1") ? $passCnt + 1 : $passCnt;
                }
                $resultReport->free();
                $stuHyouka = str_replace("評価置換", $passCnt, $stuHyouka)."\n";
                $stuUketuke = $stuUketuke."\n";
                $contents .= $stuHyouka.$stuUketuke;

            }
            $result->free();
            $contents .= "\n";
            $contents .= "\n";
            $contents .= "\n";
            $contents .= "\n";
            $data = true;
        }

        //DB切断
        Query::dbCheckIn($db);
        //CSV出力
        if ($data) {
            $filename = sprintf("%s.csv", $this->year . "科目別スクーリング・レポート状況");
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

}
?>
