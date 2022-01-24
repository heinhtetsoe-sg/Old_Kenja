<?php

class knjl416hModel extends Model
{
    public $schoolKind;
    public $cmd;

    /**
     * メンバ変数の初期化
     */
    public function knjl416hModel()
    {
    }

    public function init()
    {
        if (VARS::request("PROGRAMID")) {
            unset($this->testdiv);
            common::GetControlMaster_Fnc($this->control);
        }

        $this->cmd = VARS::request("cmd");

        //入試年度
        $this->examyear = CTRL_YEAR + 1;
        //入試制度
        $this->applicantdiv = VARS::post("APPLICANTDIV");
        //入試区分
        $this->testdiv  = VARS::post("TESTDIV");
        //受験型
        $this->examtype = VARS::post("EXAM_TYPE");
        //受験コース
        $this->course   = VARS::post("EXAMCOURSECD");
        //出願区分
        $this->shdiv    = VARS::post("SHDIV");
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    public function getDownloadModel()
    {
        $db = Query::dbCheckOut();

        //エラー項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        /****************/
        /*  ヘッダー例  */
        /****************/

        $header = array();
        $header["EXAMNO"]               = "受験番号";
        $header["NAME"]                 = "出願者氏名";
        $header["NAME_KANA"]            = "振り仮名";
        $header["SEX_NAME"]             = "性別";
        $header["BIRTHDAY"]             = "生年月日";
        $header["NAISHIN_9"]            = "9科内申";
        $header["NAISHIN_5"]            = "5科内申";
        $header["NAISHIN_3YEARS"]       = "3年間内申";
        $header["NAISHIN_NAIDAKU"]      = "内申内諾";
        $header["DISP_NAISHIN_SCHOLAR"] = "内申奨学金";
        $header["OPT_A"]                = "OPT A";
        $header["OPT_B"]                = "OPT B";
        $header["OPT_C"]                = "OPT C";
        $header["OPT_NAIDAKU"]          = "OPT内諾";
        $header["DISP_OPT_SCHOLAR"]     = "OPT奨学金";
        $header["DISP_MOSHI_NAME"]      = "在籍塾";
        $header["MOSHI_SCORE_STR"]      = "模試成績";
        $header["MOSHI_NAIDAKU"]        = "塾内諾";
        $header["DISP_MOSHI_SCHOLAR"]   = "模試奨学金";
        $header["DISP_RECOM"]           = "推薦";
        $header["DISP_KYOUKA_RECOM"]    = "強化クラブ推薦";
        $header["OTHER"]                = "その他（資・宗）";
        $header["HOPE_ORDER2"]          = "第2希望";
        $header["HOPE_ORDER3"]          = "第3希望";
        $header["TESTDIV1_EXAMNO"]      = "1回目番号";
        $header["TESTDIV2_EXAMNO"]      = "2回目番号";
        $header["TESTDIV3_EXAMNO"]      = "3回目番号";
        $header["TESTDIV4_EXAMNO"]      = "4回目番号";
        $header["TESTDIV5_EXAMNO"]      = "5回目番号";


        /********************/
        /*  データ部分出力  */
        /********************/

        $bodyContents  = "";
        $header_data = false;   //出力フラグ

        //マスタ情報取得
        $this->loadMstInfo($db);

        $query = knjl416hQuery::getSelectQuery($this);

        if ($query) {
            $count = 0;
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace("\r\n", "", str_replace(",", ".", $row));
                $array = $header;
                    
                //前処理
                $this->preprocess($row);

                //ヘッダ順に値をセット
                $sep = "";
                foreach ($array as $field => $label) {
                    $bodyContents .= $sep.$row[$field];
                    $sep = ",";
                }
                $bodyContents .= "\n";

                $header_data = true;
                $count++;
            }
            $result->free();
        }

        /**********************/
        /*  ヘッダー部分出力  */
        /**********************/

        $l003Row     = $db->getRow(knjl416hQuery::getNameMst($this, "L003", $this->applicantdiv), DB_FETCHMODE_ASSOC);
        $examTypeRow = $db->getRow(knjl416hQuery::getEntexamExamTypeMst($this, $this->examtype), DB_FETCHMODE_ASSOC);
        $courseRow   = $db->getRow(knjl416hQuery::getEntexamCourseMst($this, $this->course), DB_FETCHMODE_ASSOC);

        $header1Contents  = "{$l003Row["NAME1"]}入試出願者台帳,";
        $header1Contents .= "{$this->examyear}年度　{$examTypeRow["LABEL"]}".str_repeat(",", 11);
        $header1Contents .= "{$courseRow["LABEL"]}".str_repeat(",", 8);
        $header1Contents .= "作成日時：".date("Y/m/d　H:i");
        $header1Contents .= "\n";
        $header1Contents .= "{$count}名";
        $header1Contents .= "\n";
        
        $header2Contents  = implode(",", $header);
        $header2Contents .= "\n";

        $contents = $header1Contents.$header2Contents.$bodyContents;

        /**************************/
        /*  ファイル名用名称取得  */
        /**************************/
        //ファイル名
        $date = date("YmdHis");
        $title = $this->examyear."年度_第{$this->testdiv}回_{$l003Row["NAME1"]}入試出願者台帳_{$date}";

        Query::dbCheckIn($db);
        //ＣＳＶ出力
        if ($header_data) {
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    /********************/
    /*  マスタ情報取得   */
    /********************/

    public function loadMstInfo($db)
    {
        //コースマスタ情報取得
        $query = knjl416hQuery::getEntexamCourseMst($this);
        $this->courseMst = $this->getMstInfo($db, $query);

        //出願情報取得
        $query = knjl416hQuery::getSettingMst($this, "L006");
        $this->settingMstL006 = $this->getMstInfo($db, $query);

        //模試名取得
        $query = knjl416hQuery::getSettingMst($this, "L100");
        $this->settingMstL100 = $this->getMstInfo($db, $query);

        //塾情報取得
        $query = knjl416hQuery::getPriSchoolName($this);
        $this->preSchoolMst = $this->getMstInfo($db, $query);

        //教室情報取得
        $query = knjl416hQuery::getPriSchoolClassName($this);
        $this->preSchoolClassMst = $this->getMstInfo($db, $query);
    }

    //マスタ情報を配列に格納する関数
    public function getMstInfo($db, $query)
    {
        $mstInfo = array();
    
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $mstInfo[$row["KEY"]] = $row["VAL"];
        }
    
        return $mstInfo;
    }
    
    /***********************/
    /*  取込データ整形処理   */
    /***********************/

    public function preprocess(&$row)
    {
        //内諾表示文字列
        $row["NAISHIN_NAIDAKU"]      = $this->getCourseShdivStr($row["NAISHIN_NAIDAKU_COURSE"], $row["NAISHIN_NAIDAKU_SHDIV"]); //内申内諾
        $row["OPT_NAIDAKU"]          = $this->getCourseShdivStr($row["OPT_NAIDAKU_COURSE"], $row["OPT_NAIDAKU_SHDIV"]);         //OPT内諾
        $row["MOSHI_NAIDAKU"]        = $this->getCourseShdivStr($row["MOSHI_NAIDAKU_COURSE"], $row["MOSHI_NAIDAKU_SHDIV"]);     //塾内諾
        //第2希望・第3希望表示文字列
        $row["HOPE_ORDER2"]          = $this->getCourseShdivStr($row["HOPE_ORDER2_COURSE"], $row["HOPE_ORDER2_SHDIV"]); //第2希望
        $row["HOPE_ORDER3"]          = $this->getCourseShdivStr($row["HOPE_ORDER3_COURSE"], $row["HOPE_ORDER3_SHDIV"]); //第3希望

        //性別取得
        $row["SEX_NAME"] = ($row["SEX"] == "1") ? "男" : "女";

        //日付変換
        $row["BIRTHDAY"] = str_replace("-", "/", $row["HIHUN_BIRTHDAY"]);

        //在籍塾文字列
        $preSchoolName     = $this->preSchoolMst[$row["PS_CD"]];
        $preSchoolClsName  = $this->preSchoolClassMst[$row["PS_CD"]."-".$row["PS_CLS_CD"]];
        $row["DISP_JYUKU_NAME"] = $row["PS_CD"]." ".$preSchoolName." ".$row["PS_CLS_CD"]." ".$preSchoolClsName;

        //模試成績(模試結果+模試名の1文字目)
        $moshiName = $this->settingMstL100[$row["MOSHI_CD"]];
        if ($row["MOSHI_SCORE"] != "" && $moshiName != "") {
            $row["MOSHI_SCORE_STR"] = mb_substr($moshiName, 0, 1).$row["MOSHI_SCORE"];
        }

        //奨学金表示文字列
        $row["DISP_NAISHIN_SCHOLAR"] = $this->getScholarStr($row["NAISHIN_SCHOLAR_DIV"]);
        $row["DISP_OPT_SCHOLAR"]     = $this->getScholarStr($row["OPT_SCHOLAR_DIV"]);
        $row["DISP_MOSHI_SCHOLAR"]   = $this->getScholarStr($row["MOSHI_SCHOLAR_DIV"]);

        //推薦
        $row["DISP_RECOM"]         = ($row["HOPE_ORDER1_SHDIV"] == "1") ? "推薦" : "";
        //強化クラブ推薦
        $row["DISP_KYOUKA_RECOM"]  = ($row["HOPE_ORDER1_SHDIV"] == "1" && $row["HOPE_ORDER1_COURSE"] == $row["KYOUKA_RECOM_NAIDAKU_COURSE"]) ? "推薦" : "";

        //その他(資・宗)
        $otherStr = "";
        $spaceStr = " "."　"." "; //※半角+全角+半角スペース
        $remark   = (trim($row["QUALIFIED_REMARK"]) != "") ? $spaceStr.$row["QUALIFIED_REMARK"] : "";
        if ($row["QUALIFIED_CHK"] == "1") {
            $otherStr = $row["QUALIFIED_NAME"].$row["QUALIFIED_RANK"].$remark;
        }
        $row["OTHER"] = $otherStr;
    }

    //コース+出願区分文字列を取得
    public function getCourseShdivStr($coursecd, $shdiv)
    {
        $courseAbbv = $this->courseMst[$coursecd];
        $shdivAbbv  = $this->settingMstL006[$shdiv];
        return $courseAbbv.$shdivAbbv;
    }

    //奨学金文字列を取得
    public function getScholarStr($scholarDiv)
    {
        $scholarStr = "";
        if ($scholarDiv == "1") {
            $scholarStr = "全額";
        } elseif ($scholarDiv == "2") {
            $scholarStr = "半額";
        }
        return $scholarStr;
    }
}
