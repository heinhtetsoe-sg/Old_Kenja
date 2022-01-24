<?php

require_once('for_php7.php');
class knjl073yModel extends Model
{
    public $field      = array();
    public $ObjYear;
    public $cmd;
    public $control;   //コントロールマスタ
    public $lastColumn;//CSVの最後の列の値

    public function knjl073yModel()
    {
    }

    public function init()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear      = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),     //入試制度コンボ
                             "TESTDIV"      =>  VARS::post("TESTDIV"),          //入試区分コンボ
                            );

        $this->cmd = VARS::request("cmd");
    }

    public function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //ヘッダ(中高共通)
        $header = array("EXAMNO"            => "ログインID",
                        "PASSWORD"          => "パスワード",
                        "JUDGE_STATUS"      => "合否ステータス",
                        "REMARK1"           => "備考1",
                        "REMARK2"           => "備考2",
                        );

        $contents = "";
        $contents = implode($header, ",") ."\n";

        //合格扱いの合否区分コードを取得
        $this->passCdL013 = array();
        $query = knjl073yQuery::getNameMst("L013", $this->ObjYear);

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["NAMESPARE1"] == "1") {
                $this->passCdL013[] = $row["VALUE"];
            }
        }

        //CSVデータ取得
        $dataFlg  = false;
        if ($this->field["APPLICANTDIV"] == "1") {
            $query  = knjl073yQuery::getCsvQuery_j($this);
        } else {
            $query  = knjl073yQuery::getCsvQuery_h($this);
        }
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace("\"", "", $row);//データ内にダブルクォーテーションがあればブランクに置換
            $row = str_replace("\r", "", $row);//データ内に改行があればブランクに置換
            $row = str_replace("\n", "", $row);//データ内に改行があればブランクに置換

            $row["PASSWORD"] = str_replace("-", "", $row["BIRTHDAY"]); //パスワード

            //合否ステータス取得//
            if ($this->field["APPLICANTDIV"] == "1") {
                $judgeStatus = $this->getJudgeStatus_j($row);
            } elseif ($this->field["APPLICANTDIV"] == "2") {
                $judgeStatus = $this->getJudgeStatus_h($row);
            }

            $row["JUDGE_STATUS"] = $judgeStatus; //合否ステータス

            $row["REMARK1"] = ""; //備考1
            $row["REMARK2"] = ""; //備考2

            $sep = "";
            foreach ($header as $key => $val) {
                $contents .= $sep.$row[$key];
                $sep = ",";
            }
            $contents .= "\n";

            $dataFlg = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        //CSV出力
        if (count($this->passCdL013) == 0) {
            $this->setWarning("MSG305", "名称マスタに合格区分が設定されていません。"); //合否区分に合格設定が無い場合
            return false;
        } elseif (!$dataFlg) {
            $this->setWarning("MSG303");    //データは存在していません。
            return false;
        } else {
            //ファイル名
            $schkindName = ($this->field["APPLICANTDIV"] == "1") ? "中" : "高";
            $dateName = substr(CTRL_DATE, 0, 4).substr(CTRL_DATE, 5, 2).substr(CTRL_DATE, 8, 2);
            $outputName  = "合否M_kyoaij_{$schkindName}_{$dateName}";
            $filename = $outputName.".csv";
            //ファイルダウンロード

            common::downloadContents($contents, $filename);
            return true;
        }
    }

    //合否ステータス取得(中学)
    public function getJudgeStatus_j($row)
    {
        if ($row["JUDGEMENT"] == "4") {
            $judgeStatus = "99";
        } elseif ($row["TESTDIV"] == "1") {
            $judgeStatus = (in_array($row["JUDGEMENT"], $this->passCdL013)) ? "1" : (($row["JUDGEMENT"] == "2") ? "9" : $judgeStatus);
        } elseif ($row["TESTDIV"] == "2") {
            $judgeStatus = (in_array($row["JUDGEMENT"], $this->passCdL013) && $row["SHDIV"] == "1") ? "11" : $judgeStatus;
            $judgeStatus = (in_array($row["JUDGEMENT"], $this->passCdL013) && $row["SHDIV"] == "2") ? "12" : $judgeStatus;
            $judgeStatus = ($row["JUDGEMENT"] == "2") ? "19" : $judgeStatus;
        } elseif ($row["TESTDIV"] == "3") {
            $judgeStatus = (in_array($row["JUDGEMENT"], $this->passCdL013) && $row["SHDIV"] == "1") ? "31" : $judgeStatus;
            $judgeStatus = (in_array($row["JUDGEMENT"], $this->passCdL013) && $row["SHDIV"] == "2") ? "32" : $judgeStatus;
            $judgeStatus = ($row["JUDGEMENT"] == "2") ? "39" : $judgeStatus;
        } elseif ($row["TESTDIV"] == "4") {
            $judgeStatus = (in_array($row["JUDGEMENT"], $this->passCdL013) && $row["SHDIV"] == "1") ? "41" : $judgeStatus;
            $judgeStatus = (in_array($row["JUDGEMENT"], $this->passCdL013) && $row["SHDIV"] == "2") ? "42" : $judgeStatus;
            $judgeStatus = ($row["JUDGEMENT"] == "2") ? "49" : $judgeStatus;
        } elseif ($row["TESTDIV"] == "5") {
            $judgeStatus = (in_array($row["JUDGEMENT"], $this->passCdL013) && $row["SHDIV"] == "1") ? "21" : $judgeStatus;
            $judgeStatus = (in_array($row["JUDGEMENT"], $this->passCdL013) && $row["SHDIV"] == "2") ? "22" : $judgeStatus;
            $judgeStatus = ($row["JUDGEMENT"] == "2") ? "29" : $judgeStatus;
        }

        return $judgeStatus;
    }

    //合否ステータス取得(高校)
    public function getJudgeStatus_h($row)
    {
        //入試区分
        $testDivArr = array("GAKUGYOU_TOKUBETU" => "1", "SUISEN" => "2", "IPPAN" => "3");
        //志望区分
        $desireDivArr = array("HUTU_TOKU" => "1", "HUTU" => "2", "ENG_TOKU" => "3", "ENG" => "4");
        //推薦区分
        $recomKindArr = array("NULL" => "0", "GAKU_RECOM" => "1", "SPORT_RECOM" => "2", "ENG_RECOM" => "3", "CULTURE_RECOM" => "4", "KYOUKAI_RECOM" => "5");
        //合否区分
        $passDivArr = array("GOUKAKU" => "1", "HUGOUKAKU" => "2", "SLIDE_GOUKAKU" => "3", "KESSEKI" => "4", "TOKUBETU_GOUKAKU" => "5");
        //特待区分
        $shShipDivArr = array("NULL"      => "0",
                              "GAKU_S"    => "1",
                              "GAKU_A"    => "2",
                              "GAKU_B"    => "3",
                              "GAKU_C"    => "4",
                              "GAKU_D"    => "5",
                              "SPORT_A"   => "6",
                              "SPORT_C"   => "7",
                              "GAKU_T"    => "8",
                            );

        $mapping = array();


        //合格コース取得
        $sucCourseCd = $row["SUC_COURSECD"].$row["SUC_MAJORCD"].$row["SUC_COURSECODE"];

        /*** 推薦入試 ***/
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["GAKU_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_A"]] = "3101";
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["GAKU_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_B"]] = "3102";
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["GAKU_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_C"]] = "3103";
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["GAKU_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_D"]] = "3104";
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["GAKU_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_T"]] = "3105";
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["GAKU_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["NULL"]]   = ($row["SPORTS_FLG"] == "1") ? "3107" : "3106";

        $mapping[$testDivArr["SUISEN"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["SPORT_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["SPORT_A"]] = "3113";
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["SPORT_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["SPORT_C"]] = "3114";
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["SPORT_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_T"]]  = "3115";
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["SPORT_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["NULL"]]    = ($row["SPORTS_FLG"] == "1") ? "3116" : "3117";

        if ($row["INTERVIEW_ATTEND_FLG"] == "1") {
            $mapping[$testDivArr["SUISEN"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["GAKU_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_T"]] = "3155";
            $mapping[$testDivArr["SUISEN"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["GAKU_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["NULL"]]   = ($row["SPORTS_FLG"] == "1") ? "3157" : "3156";
        }
       
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["KYOUKAI_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_T"]] = "3167";
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["KYOUKAI_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["NULL"]]   = ($row["SPORTS_FLG"] == "1") ? "3169" : "3168";

        $mapping[$testDivArr["SUISEN"]][$desireDivArr["HUTU"]][$recomKindArr["GAKU_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_A"]] = "2101";
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["HUTU"]][$recomKindArr["GAKU_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_B"]] = "2102";
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["HUTU"]][$recomKindArr["GAKU_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_C"]] = "2103";
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["HUTU"]][$recomKindArr["GAKU_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_D"]] = "2104";
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["HUTU"]][$recomKindArr["GAKU_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_T"]] = "2105";
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["HUTU"]][$recomKindArr["GAKU_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["NULL"]]   = ($row["SPORTS_FLG"] == "1") ? "2107" : "2106";

        $mapping[$testDivArr["SUISEN"]][$desireDivArr["HUTU"]][$recomKindArr["SPORT_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["SPORT_A"]] = "2113";
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["HUTU"]][$recomKindArr["SPORT_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["SPORT_C"]] = "2114";
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["HUTU"]][$recomKindArr["SPORT_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_T"]]  = "2115";
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["HUTU"]][$recomKindArr["SPORT_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["NULL"]]    = ($row["SPORTS_FLG"] == "1") ? "2116" : "2117";

        if ($row["INTERVIEW_ATTEND_FLG"] == "1") {
            $mapping[$testDivArr["SUISEN"]][$desireDivArr["HUTU"]][$recomKindArr["GAKU_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_T"]] = "2155";
            $mapping[$testDivArr["SUISEN"]][$desireDivArr["HUTU"]][$recomKindArr["GAKU_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["NULL"]]   = ($row["SPORTS_FLG"] == "1") ? "2157" : "2156";
        }

        $mapping[$testDivArr["SUISEN"]][$desireDivArr["HUTU"]][$recomKindArr["KYOUKAI_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_T"]] = "2167";
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["HUTU"]][$recomKindArr["KYOUKAI_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["NULL"]]   = ($row["SPORTS_FLG"] == "1") ? "2169" : "2168";

        $mapping[$testDivArr["SUISEN"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["GAKU_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_A"]] = "3175";
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["GAKU_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_B"]] = "3176";
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["GAKU_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_C"]] = "3177";
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["GAKU_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_D"]] = "3178";
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["GAKU_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_T"]] = "3179";
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["GAKU_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["NULL"]]   = ($row["SPORTS_FLG"] == "1") ? "3181" : "3180";

        $mapping[$testDivArr["SUISEN"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["SPORT_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["SPORT_A"]] = "3187";
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["SPORT_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["SPORT_C"]] = "3188";
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["SPORT_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_T"]]  = "3189";
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["SPORT_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["NULL"]]    = ($row["SPORTS_FLG"] == "1") ? "3190" : "3191";

        if ($row["INTERVIEW_ATTEND_FLG"] == "1") {
            $mapping[$testDivArr["SUISEN"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["GAKU_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_T"]] = "3229";
            $mapping[$testDivArr["SUISEN"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["GAKU_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["NULL"]]   = ($row["SPORTS_FLG"] == "1") ? "3231" : "3230";
        }

        $mapping[$testDivArr["SUISEN"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["KYOUKAI_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_T"]] = "3241";
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["KYOUKAI_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["NULL"]]   = ($row["SPORTS_FLG"] == "1") ? "3243" : "3242";

        $mapping[$testDivArr["SUISEN"]][$desireDivArr["ENG"]][$recomKindArr["GAKU_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_A"]] = "4101";
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["ENG"]][$recomKindArr["GAKU_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_B"]] = "4102";
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["ENG"]][$recomKindArr["GAKU_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_C"]] = "4103";
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["ENG"]][$recomKindArr["GAKU_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_D"]] = "4104";
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["ENG"]][$recomKindArr["GAKU_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_T"]] = "4105";
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["ENG"]][$recomKindArr["GAKU_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["NULL"]]   = ($row["SPORTS_FLG"] == "1") ? "4107" : "4106";

        $mapping[$testDivArr["SUISEN"]][$desireDivArr["ENG"]][$recomKindArr["SPORT_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["SPORT_A"]] = "4113";
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["ENG"]][$recomKindArr["SPORT_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["SPORT_C"]] = "4114";
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["ENG"]][$recomKindArr["SPORT_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_T"]]  = "4115";
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["ENG"]][$recomKindArr["SPORT_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["NULL"]]    = ($row["SPORTS_FLG"] == "1") ? "4116" : "4117";

        if ($row["INTERVIEW_ATTEND_FLG"] == "1") {
            $mapping[$testDivArr["SUISEN"]][$desireDivArr["ENG"]][$recomKindArr["GAKU_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_T"]] = "4155";
            $mapping[$testDivArr["SUISEN"]][$desireDivArr["ENG"]][$recomKindArr["GAKU_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["NULL"]]   = ($row["SPORTS_FLG"] == "1") ? "4157" : "4156";
        }

        $mapping[$testDivArr["SUISEN"]][$desireDivArr["ENG"]][$recomKindArr["KYOUKAI_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_T"]] = "4167";
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["ENG"]][$recomKindArr["KYOUKAI_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["NULL"]]   = ($row["SPORTS_FLG"] == "1") ? "4169" : "4168";

        $mapping[$testDivArr["SUISEN"]][$desireDivArr["ENG"]][$recomKindArr["ENG_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_A"]] = "4175";
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["ENG"]][$recomKindArr["ENG_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_B"]] = "4176";
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["ENG"]][$recomKindArr["ENG_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_C"]] = "4177";
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["ENG"]][$recomKindArr["ENG_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_D"]] = "4178";
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["ENG"]][$recomKindArr["ENG_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_T"]] = "4179";
        $mapping[$testDivArr["SUISEN"]][$desireDivArr["ENG"]][$recomKindArr["ENG_RECOM"]][$passDivArr["GOUKAKU"]][$shShipDivArr["NULL"]]   = "4180";

        /*** 学業特別奨学生入試 ***/
        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_S"]]   = "101";
        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_A"]]   = "102";
        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_B"]]   = "103";
        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_C"]]   = "104";
        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_D"]]   = "105";
        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_T"]]   = "106";
        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["NULL"]]     = ($row["SPORTS_FLG"] == "1") ? "112" : "107";
        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["NULL"]][$passDivArr["TOKUBETU_GOUKAKU"]][$shShipDivArr["NULL"]]   = "108";
        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["NULL"]][$passDivArr["TOKUBETU_GOUKAKU"]][$shShipDivArr["GAKU_T"]] = "109";
        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["SPORT_A"]]  = "110";
        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["SPORT_C"]]  = "111";

        if ($row["DESIREDIV"] == $desireDivArr["HUTU_TOKU"] && $sucCourseCd == "10010010") { //志望区分が普通科特進で、合格コースが普通科進学の場合
            $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["NULL"]][$passDivArr["SLIDE_GOUKAKU"]][$shShipDivArr["GAKU_T"]]    = "113";
            $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["NULL"]][$passDivArr["SLIDE_GOUKAKU"]][$shShipDivArr["NULL"]]      = ($row["SPORTS_FLG"] == "1") ? "119" : "114";
            $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["NULL"]][$passDivArr["TOKUBETU_GOUKAKU"]][$shShipDivArr["NULL"]]   = "115";
            $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["NULL"]][$passDivArr["TOKUBETU_GOUKAKU"]][$shShipDivArr["GAKU_T"]] = "116";
            $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["NULL"]][$passDivArr["SLIDE_GOUKAKU"]][$shShipDivArr["SPORT_A"]]   = "117";
            $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["NULL"]][$passDivArr["SLIDE_GOUKAKU"]][$shShipDivArr["SPORT_C"]]   = "118";
        }

        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["HUTU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_S"]]   = "201";
        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["HUTU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_A"]]   = "202";
        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["HUTU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_B"]]   = "203";
        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["HUTU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_C"]]   = "204";
        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["HUTU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_D"]]   = "205";
        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["HUTU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_T"]]   = "206";
        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["HUTU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["NULL"]]     = ($row["SPORTS_FLG"] == "1") ? "212" : "207";
        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["HUTU"]][$recomKindArr["NULL"]][$passDivArr["TOKUBETU_GOUKAKU"]][$shShipDivArr["NULL"]]   = "208";
        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["HUTU"]][$recomKindArr["NULL"]][$passDivArr["TOKUBETU_GOUKAKU"]][$shShipDivArr["GAKU_T"]] = "209";
        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["HUTU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["SPORT_A"]]  = "210";
        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["HUTU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["SPORT_C"]]  = "211";

        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_S"]]   = "301";
        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_A"]]   = "302";
        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_B"]]   = "303";
        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_C"]]   = "304";
        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_D"]]   = "305";
        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_T"]]   = "306";
        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["NULL"]]     = ($row["SPORTS_FLG"] == "1") ? "312" : "307";
        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["NULL"]][$passDivArr["TOKUBETU_GOUKAKU"]][$shShipDivArr["NULL"]]   = "308";
        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["NULL"]][$passDivArr["TOKUBETU_GOUKAKU"]][$shShipDivArr["GAKU_T"]] = "309";
        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["SPORT_A"]]  = "310";
        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["SPORT_C"]]  = "311";
        if ($row["DESIREDIV"] == $desireDivArr["ENG_TOKU"] && $sucCourseCd == "10020008") { //志望区分が英語特進で、合格コースが英語進学の場合
            $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["NULL"]][$passDivArr["SLIDE_GOUKAKU"]][$shShipDivArr["GAKU_T"]]    = "313";
            $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["NULL"]][$passDivArr["SLIDE_GOUKAKU"]][$shShipDivArr["NULL"]]      = ($row["SPORTS_FLG"] == "1") ? "319" : "314";
            $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["NULL"]][$passDivArr["TOKUBETU_GOUKAKU"]][$shShipDivArr["NULL"]]   = "315";
            $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["NULL"]][$passDivArr["TOKUBETU_GOUKAKU"]][$shShipDivArr["GAKU_T"]] = "316";
            $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["NULL"]][$passDivArr["SLIDE_GOUKAKU"]][$shShipDivArr["SPORT_A"]]   = "317";
            $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["NULL"]][$passDivArr["SLIDE_GOUKAKU"]][$shShipDivArr["SPORT_C"]]   = "318";
        }

        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["ENG"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_S"]]   = "401";
        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["ENG"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_A"]]   = "402";
        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["ENG"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_B"]]   = "403";
        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["ENG"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_C"]]   = "404";
        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["ENG"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_D"]]   = "405";
        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["ENG"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_T"]]   = "406";
        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["ENG"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["NULL"]]     = ($row["SPORTS_FLG"] == "1") ? "412" : "407";
        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["ENG"]][$recomKindArr["NULL"]][$passDivArr["TOKUBETU_GOUKAKU"]][$shShipDivArr["NULL"]]   = "408";
        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["ENG"]][$recomKindArr["NULL"]][$passDivArr["TOKUBETU_GOUKAKU"]][$shShipDivArr["GAKU_T"]] = "409";
        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["ENG"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["SPORT_A"]]  = "410";
        $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["ENG"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["SPORT_C"]]  = "411";

        if ($row["INTERVIEW_ATTEND_FLG"] == "1") {
            $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_T"]]        = "1101";
            $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["NULL"]]          = "1102";
            $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["NULL"]][$passDivArr["SLIDE_GOUKAKU"]][$shShipDivArr["GAKU_T"]]  = "1103";
            $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["NULL"]][$passDivArr["SLIDE_GOUKAKU"]][$shShipDivArr["NULL"]]    = "1104";
            $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["HUTU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_T"]]             = "1201";
            $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["HUTU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["NULL"]]               = "1202";
            $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_T"]]         = "1301";
            $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["NULL"]]           = "1302";
            $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["NULL"]][$passDivArr["SLIDE_GOUKAKU"]][$shShipDivArr["GAKU_T"]]   = "1303";
            $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["NULL"]][$passDivArr["SLIDE_GOUKAKU"]][$shShipDivArr["NULL"]]     = "1304";
            $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["ENG"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_T"]]              = "1401";
            $mapping[$testDivArr["GAKUGYOU_TOKUBETU"]][$desireDivArr["ENG"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["NULL"]]                = "1402";
        }

        /*** 一般入試 ***/
        $mapping[$testDivArr["IPPAN"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_A"]]   = "5101";
        $mapping[$testDivArr["IPPAN"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_B"]]   = "5102";
        $mapping[$testDivArr["IPPAN"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_C"]]   = "5103";
        $mapping[$testDivArr["IPPAN"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_D"]]   = "5104";
        $mapping[$testDivArr["IPPAN"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_T"]]   = "5105";
        $mapping[$testDivArr["IPPAN"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["NULL"]]     = ($row["SPORTS_FLG"] == "1") ? "5109" : "5106";
        $mapping[$testDivArr["IPPAN"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["SPORT_A"]]  = "5107";
        $mapping[$testDivArr["IPPAN"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["SPORT_C"]]  = "5108";

        $mapping[$testDivArr["IPPAN"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["NULL"]][$passDivArr["SLIDE_GOUKAKU"]][$shShipDivArr["GAKU_T"]]  = "5110";
        $mapping[$testDivArr["IPPAN"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["NULL"]][$passDivArr["SLIDE_GOUKAKU"]][$shShipDivArr["NULL"]]    = ($row["SPORTS_FLG"] == "1") ? "5114" : "5111";
        $mapping[$testDivArr["IPPAN"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["NULL"]][$passDivArr["SLIDE_GOUKAKU"]][$shShipDivArr["SPORT_A"]] = "5112";
        $mapping[$testDivArr["IPPAN"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["NULL"]][$passDivArr["SLIDE_GOUKAKU"]][$shShipDivArr["SPORT_C"]] = "5113";

        $mapping[$testDivArr["IPPAN"]][$desireDivArr["HUTU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_A"]]  = "6101";
        $mapping[$testDivArr["IPPAN"]][$desireDivArr["HUTU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_B"]]  = "6102";
        $mapping[$testDivArr["IPPAN"]][$desireDivArr["HUTU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_C"]]  = "6103";
        $mapping[$testDivArr["IPPAN"]][$desireDivArr["HUTU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_D"]]  = "6104";
        $mapping[$testDivArr["IPPAN"]][$desireDivArr["HUTU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_T"]]  = "6105";
        $mapping[$testDivArr["IPPAN"]][$desireDivArr["HUTU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["NULL"]]    = ($row["SPORTS_FLG"] == "1") ? "6109" : "6106";
        $mapping[$testDivArr["IPPAN"]][$desireDivArr["HUTU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["SPORT_A"]] = "6107";
        $mapping[$testDivArr["IPPAN"]][$desireDivArr["HUTU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["SPORT_C"]] = "6108";

        $mapping[$testDivArr["IPPAN"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_A"]]  = "7101";
        $mapping[$testDivArr["IPPAN"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_B"]]  = "7102";
        $mapping[$testDivArr["IPPAN"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_C"]]  = "7103";
        $mapping[$testDivArr["IPPAN"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_D"]]  = "7104";
        $mapping[$testDivArr["IPPAN"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_T"]]  = "7105";
        $mapping[$testDivArr["IPPAN"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["NULL"]]    = ($row["SPORTS_FLG"] == "1") ? "7109" : "7106";
        $mapping[$testDivArr["IPPAN"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["SPORT_A"]] = "7107";
        $mapping[$testDivArr["IPPAN"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["SPORT_C"]] = "7108";

        $mapping[$testDivArr["IPPAN"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["NULL"]][$passDivArr["SLIDE_GOUKAKU"]][$shShipDivArr["GAKU_T"]]  = "7110";
        $mapping[$testDivArr["IPPAN"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["NULL"]][$passDivArr["SLIDE_GOUKAKU"]][$shShipDivArr["NULL"]]    = ($row["SPORTS_FLG"] == "1") ? "7114" : "7111";
        $mapping[$testDivArr["IPPAN"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["NULL"]][$passDivArr["SLIDE_GOUKAKU"]][$shShipDivArr["SPORT_A"]] = "7112";
        $mapping[$testDivArr["IPPAN"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["NULL"]][$passDivArr["SLIDE_GOUKAKU"]][$shShipDivArr["SPORT_C"]] = "7113";

        $mapping[$testDivArr["IPPAN"]][$desireDivArr["ENG"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_A"]]  = "8101";
        $mapping[$testDivArr["IPPAN"]][$desireDivArr["ENG"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_B"]]  = "8102";
        $mapping[$testDivArr["IPPAN"]][$desireDivArr["ENG"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_C"]]  = "8103";
        $mapping[$testDivArr["IPPAN"]][$desireDivArr["ENG"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_D"]]  = "8104";
        $mapping[$testDivArr["IPPAN"]][$desireDivArr["ENG"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_T"]]  = "8105";
        $mapping[$testDivArr["IPPAN"]][$desireDivArr["ENG"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["NULL"]]    = ($row["SPORTS_FLG"] == "1") ? "8109" : "8106";
        $mapping[$testDivArr["IPPAN"]][$desireDivArr["ENG"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["SPORT_A"]] = "8107";
        $mapping[$testDivArr["IPPAN"]][$desireDivArr["ENG"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["SPORT_C"]] = "8108";

        //特進チャレンジ希望チェック有
        if ($row["SELECT_SUBCLASS_DIV"] == "1") {
            $mapping[$testDivArr["IPPAN"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["NULL"]]   = "8201";
            $mapping[$testDivArr["IPPAN"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["NULL"]]    = "8203";
        }
        //帰国生フラグ有
        if ($row["INTERVIEW_ATTEND_FLG"] == "1") {
            $mapping[$testDivArr["IPPAN"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_T"]] = "2201";
            $mapping[$testDivArr["IPPAN"]][$desireDivArr["HUTU_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["NULL"]]   = "2202";
            $mapping[$testDivArr["IPPAN"]][$desireDivArr["HUTU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_T"]]      = "2301";
            $mapping[$testDivArr["IPPAN"]][$desireDivArr["HUTU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["NULL"]]        = "2302";
            $mapping[$testDivArr["IPPAN"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_T"]]  = "2401";
            $mapping[$testDivArr["IPPAN"]][$desireDivArr["ENG_TOKU"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["NULL"]]    = "2402";
            $mapping[$testDivArr["IPPAN"]][$desireDivArr["ENG"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["GAKU_T"]]       = "2501";
            $mapping[$testDivArr["IPPAN"]][$desireDivArr["ENG"]][$recomKindArr["NULL"]][$passDivArr["GOUKAKU"]][$shShipDivArr["NULL"]]         = "2502";
        }
        
        $row["JUDGE_KIND"] = ($row["JUDGE_KIND"]) ? $row["JUDGE_KIND"] : "0"; //特奨不採用はコード0
        $row["RECOM_KIND"] = ($row["RECOM_KIND"]) ? $row["RECOM_KIND"] : "0"; //推薦区分なしはコード0
        $judgeStatus = $mapping[$row["TESTDIV"]][$row["DESIREDIV"]][$row["RECOM_KIND"]][$row["JUDGEMENT"]][$row["JUDGE_KIND"]];

        if ($judgeStatus == "") {
            //不合格
            if ($row["TESTDIV"] == $testDivArr["SUISEN"] && $row["JUDGEMENT"] == $passDivArr["HUGOUKAKU"]) {
                //推薦入試が不合格
                $judgeStatus = "4181";
            }

            if ($row["TESTDIV"] == $testDivArr["GAKUGYOU_TOKUBETU"] && $row["JUDGEMENT"] == $passDivArr["HUGOUKAKU"]) {
                if ($row["SLIDE_FLG"] == "1") { //スライド
                    if ($row["DESIREDIV"] == $desireDivArr["HUTU_TOKU"]) {
                        $judgeStatus = "1105";
                    } elseif ($row["DESIREDIV"] == $desireDivArr["ENG_TOKU"]) {
                        $judgeStatus = "1305";
                    }
                } elseif ($row["INTERVIEW_ATTEND_FLG"] == "1") { //帰国生
                    if ($row["DESIREDIV"] == $desireDivArr["HUTU"]) {
                        $judgeStatus = "1203";
                    } elseif ($row["DESIREDIV"] == $desireDivArr["ENG"]) {
                        $judgeStatus = "1403";
                    }
                } else { //その他
                    if ($row["DESIREDIV"] == $desireDivArr["HUTU_TOKU"] || $row["DESIREDIV"] == $desireDivArr["ENG_TOKU"]) {
                        $judgeStatus = "413";
                    } else {
                        $judgeStatus = "414";
                    }
                }
            }

            if ($row["TESTDIV"] == $testDivArr["IPPAN"] && $row["JUDGEMENT"] == $passDivArr["HUGOUKAKU"]) {
                if ($row["INTERVIEW_ATTEND_FLG"] == "1") { //帰国生
                    if ($row["DESIREDIV"] == $desireDivArr["HUTU_TOKU"]) {
                        $judgeStatus = "2203";
                    } elseif ($row["DESIREDIV"] == $desireDivArr["HUTU"]) {
                        $judgeStatus = "2303";
                    } elseif ($row["DESIREDIV"] == $desireDivArr["ENG_TOKU"]) {
                        $judgeStatus = "2403";
                    } elseif ($row["DESIREDIV"] == $desireDivArr["ENG"]) {
                        $judgeStatus = "2503";
                    }
                } elseif ($row["SELECT_SUBCLASS_DIV"] == "1") { //チャレンジ
                    if ($row["DESIREDIV"] == $desireDivArr["HUTU_TOKU"]) {
                        $judgeStatus = "8202";
                    } elseif ($row["DESIREDIV"] == $desireDivArr["ENG_TOKU"]) {
                        $judgeStatus = "8204";
                    }
                } else { //その他
                    if ($row["DESIREDIV"] == $desireDivArr["HUTU_TOKU"] || $row["DESIREDIV"] == $desireDivArr["ENG_TOKU"]) {
                        $judgeStatus = "8110";
                    } else {
                        $judgeStatus = "8111";
                    }
                }
            }

            //欠席
            if ($row["JUDGEMENT"] == $passDivArr["KESSEKI"]) {
                if ($row["TESTDIV"] == $testDivArr["GAKUGYOU_TOKUBETU"] || $row["TESTDIV"] == $testDivArr["SUISEN"]) {
                    $judgeStatus = "9999";
                } else {
                    $judgeStatus = "999";
                }
            }
        }

        return $judgeStatus;
    }
}
