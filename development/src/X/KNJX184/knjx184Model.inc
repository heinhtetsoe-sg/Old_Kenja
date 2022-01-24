<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjx184Model extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $file;
    var $control;           //コントロールマスタ
    var $staffcd;           //登録者コード
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    /**
     * メンバ変数の初期化
     */
    function knjx184Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //登録者コード
    }

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->field = array("TAISYOU"          =>  VARS::post("TAISYOU"),           //出力対象
                             "PAGEDIV"          =>  VARS::post("PAGEDIV"),           //要録ページ種類(1:学習, 2:所見)
                             "DATADIV"          =>  VARS::post("DATADIV"),           //データ種類
                             "OUTPUT"           =>  VARS::post("OUTPUT"),            //出力取込種別
                             "YEAR"             =>  VARS::post("YEAR"),              //年度一覧
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),    //年組一覧
                             "HEADER"           =>  VARS::post("HEADER"),            //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")          //処理名
                             );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        $this->file = VARS::file("FILE");

        //知的障害
        if ($this->field["TAISYOU"] == "1") {
            //入学時の障害の状態
            if ($this->Properties["HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_J_disability"]) {
                list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_J_disability"]);
                $this->disability_moji = (int)trim($moji);
                $this->disability_gyou = (int)trim($gyou);
            } else { 
                $this->disability_moji = 10;
                $this->disability_gyou = 12;
            }
            //学習活動
            if ($this->Properties["HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_J_disability"]) {
                list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_J_disability"]);
                $this->totalstudyact_moji = (int)trim($moji);
                $this->totalstudyact_gyou = (int)trim($gyou);
            } else { 
                $this->totalstudyact_moji = 16;
                $this->totalstudyact_gyou = 5;
            }
            //観点
            if ($this->Properties["HTRAINREMARK_DAT_VIEWREMARK_SIZE_J_disability"]) {
                list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_VIEWREMARK_SIZE_J_disability"]);
                $this->viewremark_moji = (int)trim($moji);
                $this->viewremark_gyou = (int)trim($gyou);
            } else {  
                $this->viewremark_moji = 14;
                $this->viewremark_gyou = 5;
            }
            //評価
            if ($this->Properties["HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_J_disability"]) {
                list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_J_disability"]);
                $this->totalstudyval_moji = (int)trim($moji);
                $this->totalstudyval_gyou = (int)trim($gyou);
            } else {  
                $this->totalstudyval_moji = 29;
                $this->totalstudyval_gyou = 5;
            }
            //行動の記録
            if ($this->Properties["HTRAINREMARK_DAT_BEHAVEREC_REMARK_SIZE_J_disability"]) {
                list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_BEHAVEREC_REMARK_SIZE_J_disability"]);
                $this->behaverec_remark_moji = (int)trim($moji);
                $this->behaverec_remark_gyou = (int)trim($gyou);
            } else {   
                $this->behaverec_remark_moji = 75;
                $this->behaverec_remark_gyou = 5;
            }
            //総合所見及び指導上参考となる諸事項
            if ($this->Properties["HTRAINREMARK_DAT_TOTALREMARK_SIZE_J_disability"]) {
                list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_TOTALREMARK_SIZE_J_disability"]);
                $this->totalremark_moji = (int)trim($moji);
                $this->totalremark_gyou = (int)trim($gyou);
            } else {
                $this->totalremark_moji = 75;
                $this->totalremark_gyou = 9;
            }
            //出欠の記録備考
            if ($this->Properties["HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J_disability"]) {
                list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J_disability"]);
                $this->attendrec_remark_moji = (int)trim($moji);
                $this->attendrec_remark_gyou = (int)trim($gyou);
            } else {
                $this->attendrec_remark_moji = 60;
                $this->attendrec_remark_gyou = 1;
            }

            $this->studyremark1_moji = 24;
            $this->studyremark1_gyou = 35;

        } else {
        //知的障害以外
            //入学時の障害の状態
            if ($this->Properties["HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_J"]) {
                list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_J"]);
                $this->disability_moji = (int)trim($moji);
                $this->disability_gyou = (int)trim($gyou);
            } else { 
                $this->disability_moji = 9;
                $this->disability_gyou = 15;
            }
            //学習活動
            if ($this->Properties["HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_J"]) {
                list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_J"]);
                $this->totalstudyact_moji = (int)trim($moji);
                $this->totalstudyact_gyou = (int)trim($gyou);
            } else { 
                $this->totalstudyact_moji = 7;
                $this->totalstudyact_gyou = 10;
            }
            //観点
            if ($this->Properties["HTRAINREMARK_DAT_VIEWREMARK_SIZE_J"]) {
                list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_VIEWREMARK_SIZE_J"]);
                $this->viewremark_moji = (int)trim($moji);
                $this->viewremark_gyou = (int)trim($gyou);
            } else {  
                $this->viewremark_moji = 7;
                $this->viewremark_gyou = 10;
            }
            //評価
            if ($this->Properties["HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_J"]) {
                list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_J"]);
                $this->totalstudyval_moji = (int)trim($moji);
                $this->totalstudyval_gyou = (int)trim($gyou);
            } else {  
                $this->totalstudyval_moji = 13;
                $this->totalstudyval_gyou = 10;
            }
            //自立活動の記録
            if ($this->Properties["HTRAINREMARK_DAT_INDEPENDENT_REMARK_SIZE_J"]) {
                list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_INDEPENDENT_REMARK_SIZE_J"]);
                $this->indep_remark_moji = (int)trim($moji);
                $this->indep_remark_gyou = (int)trim($gyou);
            } else {   
                $this->indep_remark_moji = 48;
                $this->indep_remark_gyou = 7;
            }
            //総合所見及び指導上参考となる諸事項
            if ($this->Properties["HTRAINREMARK_DAT_TOTALREMARK_SIZE_J"]) {
                list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_TOTALREMARK_SIZE_J"]);
                $this->totalremark_moji = (int)trim($moji);
                $this->totalremark_gyou = (int)trim($gyou);
            } else {
                $this->totalremark_moji = 60;
                $this->totalremark_gyou = 8;
            }
            //出欠の記録備考
            if ($this->Properties["HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J"]) {
                list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J"]);
                $this->attendrec_remark_moji = (int)trim($moji);
                $this->attendrec_remark_gyou = (int)trim($gyou);
            } else {
                $this->attendrec_remark_moji = 38;
                $this->attendrec_remark_gyou = 1;
            }
            //特別活動の記録の観点
            if ($this->Properties["HTRAINREMARK_DAT_SPECIALACTREMARK_SIZE_J"]) {
                list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_SPECIALACTREMARK_SIZE_J"]);
                $this->specialactremark_moji = (int)trim($moji);
                $this->specialactremark_gyou = (int)trim($gyou);
            } else { 
                $this->specialactremark_moji = 14;
                $this->specialactremark_gyou = 7;
            }
        }

    }

    function &getMainModel() {
    }

    /**
     * 実行モデルを返します
    */
    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //エラー項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");

        $header = array();
        //知的障害
        if ($this->field["TAISYOU"] == "1") {
            //学習
            if ($this->field["PAGEDIV"] == "1") {
                $header["SCHOOLCD"]         = "※本校区分";
                $header["YEAR"]             = "※年度";
                $header["SCHREGNO"]         = "※学籍番号";
                $header["NAME"]             = "氏名";
                $header["CLASSCD"]          = "※教科コード";
                $header["SCHOOL_KIND"]      = "※学校種別";
                $header["CURRICULUM_CD"]    = "※教育課程コード";
                $header["SUBCLASSCD"]       = "※科目コード";
                $header["SUBCLASSNAME"]     = "科目名";
                $header["REMARK1"]          = "文言評価";//SCHREG_STUDYREC_DETAIL_DAT(SEQ=001, REMARK1)
            } else {
            //所見
                //入学時の障害の状態
                if ($this->field["DATADIV"] == "1") {
                    $header["SCHREGNO"]                 = "※学籍番号";
                    $header["GRADE"]                    = "学年";
                    $header["HR_CLASS"]                 = "クラス";
                    $header["ATTENDNO"]                 = "出席番号";
                    $header["NAME"]                     = "氏名";
                    $header["ENT_DISABILITY_REMARK"]    = "入学時の障害の状態";//HTRAINREMARK_DETAIL2_HDAT(REMARK1, HTRAIN_SEQ=001)
                } else {
                //毎年度データ
                    $header["YEAR"]                     = "※年度";
                    $header["SCHREGNO"]                 = "※学籍番号";
                    $header["GRADE"]                    = "学年";
                    $header["HR_CLASS"]                 = "クラス";
                    $header["ATTENDNO"]                 = "出席番号";
                    $header["NAME"]                     = "氏名";
                    $header["TOTALSTUDYACT"]            = "学習活動";      //HTRAINREMARK_DAT
                    $header["VIEWREMARK"]               = "観点";          //HTRAINREMARK_DAT
                    $header["TOTALSTUDYVAL"]            = "評価";          //HTRAINREMARK_DAT
                    $header["BEHAVEREC_REMARK"]         = "行動の記録";    //HTRAINREMARK_DETAIL2_DAT(REMARK1, HTRAIN_SEQ=002)
                    $header["TOTALREMARK"]              = "総合所見";      //HTRAINREMARK_DAT(TOTALREMARK)
                    $header["ATTENDREC_REMARK"]         = "出欠の記録備考";//HTRAINREMARK_DAT(ATTENDREC_REMARK)
                }
            }
        } else {
        //知的障害以外(所見のみ)
            //入学時の障害の状態
            if ($this->field["DATADIV"] == "1") {
                $header["SCHREGNO"]                 = "※学籍番号";
                $header["GRADE"]                    = "学年";
                $header["HR_CLASS"]                 = "クラス";
                $header["ATTENDNO"]                 = "出席番号";
                $header["NAME"]                     = "氏名";
                $header["ENT_DISABILITY_REMARK"]    = "入学時の障害の状態";//HTRAINREMARK_DETAIL2_HDAT(REMARK1, HTRAIN_SEQ=001)
            } else {
            //毎年度データ
                $header["YEAR"]                     = "※年度";
                $header["SCHREGNO"]                 = "※学籍番号";
                $header["GRADE"]                    = "学年";
                $header["HR_CLASS"]                 = "クラス";
                $header["ATTENDNO"]                 = "出席番号";
                $header["NAME"]                     = "氏名";
                $header["TOTALSTUDYACT"]            = "学習活動";      //HTRAINREMARK_DAT
                $header["VIEWREMARK"]               = "観点";          //HTRAINREMARK_DAT
                $header["TOTALSTUDYVAL"]            = "評価";          //HTRAINREMARK_DAT
                $header["INDEPENDENT_REMARK"]       = "自立活動の記録";//HTRAINREMARK_DETAIL2_DAT(REMARK1, HTRAIN_SEQ=001)
                $header["TOTALREMARK"]              = "総合所見";      //HTRAINREMARK_DAT(TOTALREMARK)
                $header["ATTENDREC_REMARK"]         = "出欠の記録備考";//HTRAINREMARK_DAT(ATTENDREC_REMARK)

                $db = Query::dbCheckOut();
                //行動の記録項目取得
                $query = knjx184Query::getNameMst("D035");
                $result = $db->query($query);
                $this->arrayD035 = array();
                while ($setItem = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $this->arrayD035[$setItem["NAMECD2"]] = $setItem;
                }
                foreach ($this->arrayD035 as $key => $val) {
                    $header["RECORD_1".$key] = $val["NAME1"];//BEHAVIOR_DAT(DIV=1, CODE順)
                }
                Query::dbCheckIn($db);

                //特別活動の記録項目
                $header["RECORD201"]                = "学級活動";   //BEHAVIOR_DAT(DIV=2, CODE=01)
                $header["RECORD202"]                = "生徒会活動"; //BEHAVIOR_DAT(DIV=2, CODE=02)
                $header["RECORD203"]                = "学校行事";   //BEHAVIOR_DAT(DIV=2, CODE=03)

                //特別活動の記録の観点
                $header["SPECIALACTREMARK"]         = "特別活動の記録の観点";//HTRAINREMARK_DAT
            }
        }

        $header_rei = array();
        //知的障害
        if ($this->field["TAISYOU"] == "1") {
            //学習
            if ($this->field["PAGEDIV"] == "1") {
                $header_rei["SCHOOLCD"]         = "0:本校 1:前籍校";
                $header_rei["YEAR"]             = CTRL_YEAR;
                $header_rei["SCHREGNO"]         = "12345678";
                $header_rei["NAME"]             = "アルプ太郎";
                $header_rei["CLASSCD"]          = "13";
                $header_rei["SCHOOL_KIND"]      = "J";
                $header_rei["CURRICULUM_CD"]    = "2";
                $header_rei["SUBCLASSCD"]       = "130001";
                $header_rei["SUBCLASSNAME"]     = "国語";
                $header_rei["REMARK1"]          = "(全角".$this->studyremark1_moji."文字X".$this->studyremark1_gyou."行まで)";
            } else {
            //所見
                //入学時の障害の状態
                if ($this->field["DATADIV"] == "1") {
                    $header_rei["SCHREGNO"]                 = "12345678";
                    $header_rei["GRADE"]                    = "99";
                    $header_rei["HR_CLASS"]                 = "999";
                    $header_rei["ATTENDNO"]                 = "999";
                    $header_rei["NAME"]                     = "アルプ太郎";
                    $header_rei["ENT_DISABILITY_REMARK"]    = "(全角".$this->disability_moji."文字X".$this->disability_gyou."行まで)";
                } else {
                //毎年度データ
                    $header_rei["YEAR"]                     = CTRL_YEAR;
                    $header_rei["SCHREGNO"]                 = "12345678";
                    $header_rei["GRADE"]                    = "99";
                    $header_rei["HR_CLASS"]                 = "999";
                    $header_rei["ATTENDNO"]                 = "999";
                    $header_rei["NAME"]                     = "アルプ太郎";
                    $header_rei["TOTALSTUDYACT"]            = "(全角".$this->totalstudyact_moji."文字X".$this->totalstudyact_gyou."行まで)";
                    $header_rei["VIEWREMARK"]               = "(全角".$this->viewremark_moji."文字X".$this->viewremark_gyou."行まで)";
                    $header_rei["TOTALSTUDYVAL"]            = "(全角".$this->totalstudyval_moji."文字X".$this->totalstudyval_gyou."行まで)";
                    $header_rei["BEHAVEREC_REMARK"]         = "(全角".$this->behaverec_remark_moji."文字X".$this->behaverec_remark_gyou."行まで)";
                    $header_rei["TOTALREMARK"]              = "(全角".$this->totalremark_moji."文字X".$this->totalremark_gyou."行まで)";
                    $header_rei["ATTENDREC_REMARK"]         = "(全角".$this->attendrec_remark_moji."文字X".$this->attendrec_remark_gyou."行まで)";
                }
            }
        } else {
        //知的障害以外(所見のみ)
            //入学時の障害の状態
            if ($this->field["DATADIV"] == "1") {
                $header_rei["SCHREGNO"]                 = "12345678";
                $header_rei["GRADE"]                    = "99";
                $header_rei["HR_CLASS"]                 = "999";
                $header_rei["ATTENDNO"]                 = "999";
                $header_rei["NAME"]                     = "アルプ太郎";
                $header_rei["ENT_DISABILITY_REMARK"]    = "(全角".$this->disability_moji."文字X".$this->disability_gyou."行まで)";
            } else {
                //毎年度データ
                $header_rei["YEAR"]                     = CTRL_YEAR;
                $header_rei["SCHREGNO"]                 = "12345678";
                $header_rei["GRADE"]                    = "99";
                $header_rei["HR_CLASS"]                 = "999";
                $header_rei["ATTENDNO"]                 = "999";
                $header_rei["NAME"]                     = "アルプ太郎";
                $header_rei["TOTALSTUDYACT"]            = "(全角".$this->totalstudyact_moji."文字X".$this->totalstudyact_gyou."行まで)";
                $header_rei["VIEWREMARK"]               = "(全角".$this->viewremark_moji."文字X".$this->viewremark_gyou."行まで)";
                $header_rei["TOTALSTUDYVAL"]            = "(全角".$this->totalstudyval_moji."文字X".$this->totalstudyval_gyou."行まで)";
                $header_rei["INDEPENDENT_REMARK"]       = "(全角".$this->indep_remark_moji."文字X".$this->indep_remark_gyou."行まで)";
                $header_rei["TOTALREMARK"]              = "(全角".$this->totalremark_moji."文字X".$this->totalremark_gyou."行まで)";
                $header_rei["ATTENDREC_REMARK"]         = "(全角".$this->attendrec_remark_moji."文字X".$this->attendrec_remark_gyou."行まで)";
                //行動の記録
                foreach ($this->arrayD035 as $key => $val) {
                    $header_rei["RECORD_1".$key]            = "1";
                }
                //特別活動の記録
                $header_rei["RECORD201"]                = "1";
                $header_rei["RECORD202"]                = "1";
                $header_rei["RECORD203"]                = "1";
                //特別活動の記録の観点
                $header_rei["SPECIALACTREMARK"]         = "(全角".$this->specialactremark_moji."文字X".$this->specialactremark_gyou."行まで)";
            }
        }

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"] == "1") {
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "3") {
            //エラー
            $tables = array();
            if ($this->field["TAISYOU"] == "1") {
                if ($this->field["PAGEDIV"] == "1") {
                    $tables = array("1" => "SCHREG_STUDYREC_DETAIL_DAT");
                } else {
                    if ($this->field["DATADIV"] == "1") {
                        $tables = array("1" => "HTRAINREMARK_DETAIL2_HDAT");
                    } else {
                        $tables = array("1" => "HTRAINREMARK_DETAIL2_DAT","2" => "HTRAINREMARK_DAT");
                    }
                }
            } else {
                if ($this->field["DATADIV"] == "1") {
                    $tables = array("1" => "HTRAINREMARK_DETAIL2_HDAT");
                } else {
                    $tables = array("1" => "HTRAINREMARK_DAT","2" => "HTRAINREMARK_DETAIL2_DAT","3" => "BEHAVIOR_DAT");
                }
            }
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            $headerDupl = array();
            $headerDupl["SCHOOLCD"]      = "本校区分";
            $headerDupl["YEAR"]          = "年度";
            $headerDupl["SCHREGNO"]      = "学籍番号";
            $headerDupl["CLASSCD"]       = "教科コード";
            $headerDupl["SCHOOL_KIND"]   = "学校種別";
            $headerDupl["CURRICULUM_CD"] = "教育課程コード";
            $headerDupl["SUBCLASSCD"]    = "科目コード";
            $headerDupl["SEQ"]           = "SEQ";
            $headerDupl["HTRAIN_SEQ"]    = "SEQ";
            $headerDupl["DIV"]           = "区分";
            $headerDupl["CODE"]          = "コード";
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDupl);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($header_err, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "4") {
            //データ
            if ( $this->field["HEADER"] == "on" ) {
                $contents .= implode($header, ",")."\n";
            }
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") {
            //DB接続
            $db = Query::dbCheckOut();

            if ($this->field["OUTPUT"] == "3") {
                //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"] == "4") {
                //データ
                $query = knjx184Query::selectMainQuery($this);
            }

            //SQL文発行
            if ($query) {
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($this->field["OUTPUT"] == "4") {                //データ
                        $row = str_replace(array("\r\n", "\r", "\n"), "↓", str_replace(",", ".", $row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                        $array = $header;
                    } else {
                        $array = $header_err;
                    }

                    //ヘッダ順に値をセット
                    $comma = "";
                    foreach ($array as $key => $headerName) {
                        $contents .= $comma.$row[$key];
                        $comma = ",";
                    }
                    $contents .= "\n";

                    $header_data = true;
                }
                $result->free();
            }
            Query::dbCheckIn($db);
        }

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "3") {        //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "中学部指導要録";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }

    }

    //ＣＳＶ取込
    public function &getExecModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ( $this->IsDataMatched($this->data_arr) ) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                if ($this->field["SHORI_MEI"] == "1") {
                    knjx184Query::insertQueryCsv($this, $this->data_arr);
                } else {
                    knjx184Query::DeleteQueryCsv($this, $this->data_arr);
                }
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //取込
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            //知的障害
            if ($this->field["TAISYOU"] == "1") {
                //学習
                if ($this->field["PAGEDIV"] == "1") {
                    $checkdata["SCHOOLCD"]      = array(1,    "HISSU",  "INT",   "本校区分");
                    $checkdata["YEAR"]          = array(4,    "HISSU",  "INT",   "年度");
                    $checkdata["SCHREGNO"]      = array(8,    "HISSU",  "INT",   "学籍番号");
                    $checkdata["CLASSCD"]       = array(2,    "HISSU",  "INT",   "教科コード");
                    $checkdata["SCHOOL_KIND"]   = array(2,    "HISSU",  "",      "学校種別");
                    $checkdata["CURRICULUM_CD"] = array(1,    "HISSU",  "INT",   "教育課程コード");
                    $checkdata["SUBCLASSCD"]    = array(6,    "HISSU",  "INT2",  "科目コード");
                    $checkdata["REMARK1"]       = array(array($this->studyremark1_moji, $this->studyremark1_gyou), "",     "",   "文言評価");
                } else {
                //所見
                    //入学時の障害の状態
                    if ($this->field["DATADIV"] == "1") {
                        $checkdata["SCHREGNO"]              = array(8,    "HISSU",  "INT",   "学籍番号");
                        $checkdata["ENT_DISABILITY_REMARK"] = array(array($this->disability_moji, $this->disability_gyou), "",       "",      "入学時の障害の状態");
                    } else {
                    //毎年度データ
                        $checkdata["YEAR"]              = array(4,    "HISSU",  "INT",   "年度");
                        $checkdata["SCHREGNO"]          = array(8,    "HISSU",  "INT",   "学籍番号");
                        $checkdata["TOTALSTUDYACT"]     = array(array($this->totalstudyact_moji, $this->totalstudyact_gyou),  "",       "",      "学習活動");
                        $checkdata["VIEWREMARK"]        = array(array($this->viewremark_moji, $this->viewremark_gyou),  "",       "",      "観点");
                        $checkdata["TOTALSTUDYVAL"]     = array(array($this->totalstudyval_moji, $this->totalstudyval_gyou),  "",       "",      "評価");
                        $checkdata["BEHAVEREC_REMARK"]  = array(array($this->behaverec_remark_moji, $this->behaverec_remark_gyou),  "",       "",      "行動の記録");
                        $checkdata["TOTALREMARK"]       = array(array($this->totalremark_moji, $this->totalremark_gyou), "",       "",      "総合所見");
                        $checkdata["ATTENDREC_REMARK"]  = array(array($this->attendrec_remark_moji, $this->attendrec_remark_gyou),  "",       "",      "出欠の記録備考");
                    }
                }
            } else {
            //知的障害以外(所見のみ)
                //入学時の障害の状態
                if ($this->field["DATADIV"] == "1") {
                    $checkdata["SCHREGNO"]              = array(8,    "HISSU",  "INT",   "学籍番号");
                    $checkdata["ENT_DISABILITY_REMARK"] = array(array($this->disability_moji, $this->disability_gyou), "",       "",      "入学時の障害の状態");
                } else {
                //毎年度データ
                    $checkdata["YEAR"]                  = array(4,    "HISSU",  "INT",   "年度");
                    $checkdata["SCHREGNO"]              = array(8,    "HISSU",  "INT",   "学籍番号");
                    $checkdata["TOTALSTUDYACT"]         = array(array($this->totalstudyact_moji, $this->totalstudyact_gyou),  "",       "",      "学習活動");
                    $checkdata["VIEWREMARK"]            = array(array($this->viewremark_moji, $this->viewremark_gyou),  "",       "",      "観点");
                    $checkdata["TOTALSTUDYVAL"]         = array(array($this->totalstudyval_moji, $this->totalstudyval_gyou),  "",       "",      "評価");
                    $checkdata["INDEPENDENT_REMARK"]    = array(array($this->indep_remark_moji, $this->indep_remark_gyou), "",       "",      "自立活動の記録");
                    $checkdata["TOTALREMARK"]           = array(array($this->totalremark_moji, $this->totalremark_gyou), "",       "",      "総合所見");
                    $checkdata["ATTENDREC_REMARK"]      = array(array($this->attendrec_remark_moji, $this->attendrec_remark_gyou),  "",       "",      "出欠の記録備考");
                    //行動の記録項目取得
                    foreach ($this->arrayD035 as $key => $val) {
                        $setArr1   = array();
                        $setArr1[] = "1";
                        $setArr1[] = "FLAG";
                        $setArr1[] = "INT";
                        $setArr1[] = $val["NAME1"];
                        $checkdata["RECORD_1".$key]         = $setArr1;
                    }
                    //特別活動の記録項目取得
                    $checkdata["RECORD201"]             = array(1,    "FLAG",   "INT",   "学級活動");
                    $checkdata["RECORD202"]             = array(1,    "FLAG",   "INT",   "生徒会活動");
                    $checkdata["RECORD203"]             = array(1,    "FLAG",   "INT",   "学校行事");
                    //特別活動の記録の観点
                    $checkdata["SPECIALACTREMARK"]      = array(array($this->specialactremark_moji, $this->specialactremark_gyou),  "",       "",      "特別活動の記録の観点");
                }
            }

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1   = array();
            $primaryKey2   = array();
            $primaryKey3   = array();
            $duplicateCsv1 = array();
            $duplicateCsv2 = array();
            $duplicateCsv3 = array();
            if ($this->field["TAISYOU"] == "1") {
                if ($this->field["PAGEDIV"] == "1") {
                    $primaryKey1 = $this->getPrimaryKey('SCHREG_STUDYREC_DETAIL_DAT');
                } else {
                    if ($this->field["DATADIV"] == "1") {
                        $primaryKey1 = $this->getPrimaryKey('HTRAINREMARK_DETAIL2_HDAT');
                    } else {
                        $primaryKey1 = $this->getPrimaryKey('HTRAINREMARK_DETAIL2_DAT');
                        $primaryKey2 = $this->getPrimaryKey('HTRAINREMARK_DAT');
                    }
                }
            } else {
                if ($this->field["DATADIV"] == "1") {
                    $primaryKey1 = $this->getPrimaryKey('HTRAINREMARK_DETAIL2_HDAT');
                } else {
                    $primaryKey1 = $this->getPrimaryKey('HTRAINREMARK_DAT');
                    $primaryKey2 = $this->getPrimaryKey('HTRAINREMARK_DETAIL2_DAT');
                    $primaryKey3 = $this->getPrimaryKey('BEHAVIOR_DAT');
                }
            }
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADER"] == "on") {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    $tmp    = str_replace("↓", "\n", $tmp);
                    //配列の要素数チェック
                    $checkcount = get_count($tmp);

                    //知的障害
                    if ($this->field["TAISYOU"] == "1") {
                        //学習
                        if ($this->field["PAGEDIV"] == "1") {
                            $data["SCHOOLCD"]           = $tmp[0];
                            $data["YEAR"]               = $tmp[1];
                            $data["SCHREGNO"]           = $tmp[2];
                            $data["CLASSCD"]            = $tmp[4];
                            $data["SCHOOL_KIND"]        = $tmp[5];
                            $data["CURRICULUM_CD"]      = $tmp[6];
                            $data["SUBCLASSCD"]         = $tmp[7];
                            $data["REMARK1"]            = $tmp[9];

                            $dataCnt = 10;
                        } else {
                        //所見
                            //入学時の障害の状態
                            if ($this->field["DATADIV"] == "1") {
                                $data["SCHREGNO"]               = $tmp[0];
                                $data["ENT_DISABILITY_REMARK"]  = $tmp[5];

                                $dataCnt = 6;
                            } else {
                            //毎年度データ
                                $data["YEAR"]               = $tmp[0];
                                $data["SCHREGNO"]           = $tmp[1];
                                $data["TOTALSTUDYACT"]      = $tmp[6];
                                $data["VIEWREMARK"]         = $tmp[7];
                                $data["TOTALSTUDYVAL"]      = $tmp[8];
                                $data["BEHAVEREC_REMARK"]   = $tmp[9];
                                $data["TOTALREMARK"]        = $tmp[10];
                                $data["ATTENDREC_REMARK"]   = $tmp[11];

                                $dataCnt = 12;
                            }
                        }
                    } else {
                    //知的障害以外(所見のみ)
                        //入学時の障害の状態
                        if ($this->field["DATADIV"] == "1") {
                            $data["SCHREGNO"]               = $tmp[0];
                            $data["ENT_DISABILITY_REMARK"]  = $tmp[5];

                            $dataCnt = 6;
                        } else {
                        //毎年度データ
                            $data["YEAR"]               = $tmp[0];
                            $data["SCHREGNO"]           = $tmp[1];
                            $data["TOTALSTUDYACT"]      = $tmp[6];
                            $data["VIEWREMARK"]         = $tmp[7];
                            $data["TOTALSTUDYVAL"]      = $tmp[8];
                            $data["INDEPENDENT_REMARK"] = $tmp[9];
                            $data["TOTALREMARK"]        = $tmp[10];
                            $data["ATTENDREC_REMARK"]   = $tmp[11];
                            //行動の記録項目
                            $cnt035 = 11;
                            foreach ($this->arrayD035 as $key => $val) {
                                $data["RECORD_1".$key]      = $tmp[++$cnt035];
                            }
                            //特別活動の記録項目
                            $data["RECORD201"]          = $tmp[++$cnt035];
                            $data["RECORD202"]          = $tmp[++$cnt035];
                            $data["RECORD203"]          = $tmp[++$cnt035];
                            //特別活動の記録の観点
                            $data["SPECIALACTREMARK"]   = $tmp[++$cnt035];

                            $dataCnt = 1 + $cnt035;
                        }
                    }

                    //項目数によるファイルチェック
                    if ($checkcount != $dataCnt) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $primaryKey1, $primaryKey2, $primaryKey3)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
                    }
                    foreach ($primaryKey2 as $key => $val) {
                        $duplicateCsv2[$line][$val] = $data[$val];
                    }
                    foreach ($primaryKey3 as $key => $val) {
                        $duplicateCsv3[$line][$val] = $data[$val];
                    }
                    $line++;
                }
            }
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        }

        return $temp_arr;
    }

    //エラーチェック
    public function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $primaryKey1, $primaryKey2, $primaryKey3)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $masterErrFlg       = false;    //マスタエラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $valuesErrFlg       = false;    //値エラーフラグ
        $duplicateCsvErrFlg = false;    //重複エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $masterErrMsg       = "";       //マスタエラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $valuesErrMsg       = "";       //値エラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

        $hissuSep        = "";
        $masterSep       = "";
        $numericSep      = "";
        $lengthSep       = "";
        $valuesSep       = "";
        $duplicateCsvSep = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }
        $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data);
        if ($dupFlg2[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg2[1] ."行目)");
        }
        $dupFlg3 = $this->duplicateCsvCheck($duplicateCsv3, $primaryKey3, $data);
        if ($dupFlg3[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複3(". $dupFlg3[1] ."行目)");
        }
        $checkNameMstCd = array("CURRICULUM_CD"         => "Z018");

        foreach ($data_each as $key => $val) {
            //SCHREG_REGD_DAT にその生徒が登録されているのかチェックする
            if (($key == "SCHREGNO") && (0 == $db->getOne(knjx184Query::schregNoCntSql($data[$key])))) {
                $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[3]);
            }
            //必須項目チェック
            if ($val[1] == "HISSU" && !strlen(trim($data[$key]))) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
            }
            //数値チェック
            if (($val[2] == "INT" || $val[2] == "INT2") && $data[$key] != "" && !is_numeric($data[$key])) {
                $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[3]);
            }
            //チェック用にゼロ埋め
            if ($val[2] == "INT2" && $data[$key] != "" && is_numeric($data[$key])) {
                $data[$key] = sprintf("%0{$val[0]}d", $data[$key]);
            }
            /********************/
            /**  存在チェック  **/
            /********************/
            //SCHOOLCDは"0"か"1"
            if (($key == "SCHOOLCD") && (!in_array($data[$key], array("0", "1")))) {
                $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[3]);
            }
            //年度
            if ($key == "YEAR" && CTRL_YEAR != $data[$key]) {
                $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[3]);
            }
            //名称マスタ
            if ($checkNameMstCd[$key] && $data[$key] != "") {
                $query = knjx184Query::nameCntSql($checkNameMstCd[$key], $data[$key]);
                $rowCount = $db->getOne($query);
                if ($rowCount == "0") {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[3]);
                }
            }
            //教科コード
            if (in_array($key, array('CLASSCD')) && strlen($data[$key]) && 0 == $db->getOne(knjx184Query::classcdCntSql($data[$key]))) {
                $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[3]);
            }
            //科目コード
            if (in_array($key, array('SUBCLASSCD')) && strlen($data[$key]) && 0 == $db->getOne(knjx184Query::subclassCntSql($data, $data[$key]))) {
                $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[3]);
            }

            //校種チェック(J固定)
            if (in_array($key, array('SCHOOL_KIND')) && strlen($data[$key]) && ($data[$key] != "J")) {
                $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[3]);
            }

            if ($this->field["SHORI_MEI"] == "1") {
                //文字数チェック
                if (is_array($val[0])) {
                    $moji = $val[0][0];
                    $gyou = $val[0][1];
                    if ($this->validate_row_cnt($data[$key], $moji * 2) > $gyou) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]);
                    }
                } else {
                    if (strlen($data[$key]) > $val[0]) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]);
                    }
                }

                //フラグチェック(値は1かNULL)
                if ($val[1] == "FLAG" && !($data[$key] == "1" || !strlen(trim($data[$key])))) {
                    $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[3]);
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$masterErrMsg.$valuesErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx184Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }


    //データの整合性チェック
    function IsDataMatched(&$data_arr) {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915","処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;

    }
}
?>
