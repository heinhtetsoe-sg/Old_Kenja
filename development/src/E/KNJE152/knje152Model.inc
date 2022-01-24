<?php

require_once('for_php7.php');

class knje152Model extends Model
{
    public $field = array();
    public $cmd;
    public $control;   //コントロールマスタ
    public $schoolCd;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限

    public function knje152Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
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

        $this->field = array("YEAR"         =>  VARS::post("YEAR"),         //年度
                             "GRADE"        =>  VARS::post("GRADE"),        //学年
                             "OUTPUT"       =>  VARS::post("OUTPUT"),       //出力形態
                             "DBNAME"       =>  VARS::post("DBNAME")        //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
    }

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        if ($this->cmd == "csv1") {
            return $this->printStudyrec();
        } else {
            return $this->printStudyrec2();
        }
    }

    //ＣＳＶ出力（基本情報）
    public function printStudyrec()
    {
        $contents = "";

        //DB接続
        $db = Query::dbCheckOut();

        //ヘッダー
        $header = array("GRADE"         => "学年",
                        "HR_CLASS"      => "組",
                        "ATTENDNO"      => "出席番号",
                        "NAME_SHOW"     => "生徒氏名",
                        "SCHREGNO"      => "学籍番号"
                        );

        //ヘッダー（教科）
        $classCnt = 0;
        $classTemp = array();
        $query = knje152Query::getClassMst($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $header["CLASSNAME".$classCnt] = $row["CLASSNAME"];

            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                $classTemp[] = $row["CLASSCD"]."-".$row["SCHOOL_KIND"];
            } else {
                $classTemp[] = $row["CLASSCD"];
            }
            $classCnt++;
        }
        $result->free();

        //ヘッダー（全体）
        $header["CLASSNAME".($classCnt + 1)] = '全体';

        //SQL文発行
        $query = knje152Query::getStudyrecCsv($this);
        $result = $db->query($query);
        $dataTemp = array();
        $tempCnt = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            //学籍
            if ($dataTemp[$row["SCHREGNO"]] == "") {
                $dataTemp[$row["SCHREGNO"]][] = $row["GRADE"];
                $dataTemp[$row["SCHREGNO"]][] = $row["HR_CLASS"];
                $dataTemp[$row["SCHREGNO"]][] = $row["ATTENDNO"];
                $dataTemp[$row["SCHREGNO"]][] = $row["NAME_SHOW"];
                $dataTemp[$row["SCHREGNO"]][] = $row["SCHREGNO"];
            }
            $dataTemp[$row["SCHREGNO"]][] = $row["VALUATION"];

            $tempCnt[$row["SCHREGNO"]]++;
        }
        $result->free();

        //ヘッダー出力
        $contents .= implode($header, ",") ."\n";

        //データ出力
        $data = false;
        foreach ($dataTemp as $key => $val) {
            $contents .= implode($val, ",") ."\n";
            $data = true;
        }

        //ファイル名用学年取得
        $query = knje152Query::getGrade($this, $this->field["GRADE"]);
        $grade = $db->getOne($query);

        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($data) {
            $nendo = CTRL_YEAR."年度";
            $filename = sprintf("%s.csv", $nendo.$grade."基本情報");
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    //ＣＳＶ出力（科目別成績詳細情報）
    public function printStudyrec2()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //SQL文発行
        $query = knje152Query::getStudyrecCsv2($this);
        $result = $db->query($query);
        $dataTemp = array();
        $dataTemp2 = array();
        $tempCnt = array();
        $cnt = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            if ($this->field["OUTPUT"] == "1") {
                if ($dataTemp[$row["SCHREGNO"]] == "") {
                    $dataTemp[$row["SCHREGNO"]][] = $row["GRADE"];
                    $dataTemp[$row["SCHREGNO"]][] = $row["HR_CLASS"];
                    $dataTemp[$row["SCHREGNO"]][] = $row["ATTENDNO"];
                    $dataTemp[$row["SCHREGNO"]][] = $row["NAME_SHOW"];
                    $dataTemp[$row["SCHREGNO"]][] = $row["SCHREGNO"];
                }
                if ($row["YEAR"] == "9999") {
                    $dataTemp2[$row["SCHREGNO"]]["ABROAD_CREDITS"] = $row["GET_CREDIT"];
                } else {
                    $dataTemp[$row["SCHREGNO"]][] = $row["YEAR"];
                    $dataTemp[$row["SCHREGNO"]][] = $row["SUBCLASSNAME"];
                    $dataTemp[$row["SCHREGNO"]][] = $row["VALUATION"];
                    $dataTemp[$row["SCHREGNO"]][] = $row["GET_CREDIT"];
                    $dataTemp[$row["SCHREGNO"]][] = $row["ADD_CREDIT"];
                    $dataTemp[$row["SCHREGNO"]][] = $row["COMP_CREDIT"];

                    $tempCnt[$row["SCHREGNO"]]++;
                }
            } else {
                $dataTemp[$cnt]["GRADE"]            = $row["GRADE"];
                $dataTemp[$cnt]["HR_CLASS"]         = $row["HR_CLASS"];
                $dataTemp[$cnt]["ATTENDNO"]         = $row["ATTENDNO"];
                $dataTemp[$cnt]["NAME_SHOW"]        = $row["NAME_SHOW"];
                $dataTemp[$cnt]["SCHREGNO"]         = $row["SCHREGNO"];
                $dataTemp[$cnt]["YEAR"]             = ($row["YEAR"] == "9999") ? "" : $row["YEAR"];
                //教育課程対応
                if ($this->Properties["useCurriculumcd"] == '1') {
                    $dataTemp[$cnt]["CLASSCD"]          = $row["CLASSCD"];
                    $dataTemp[$cnt]["SCHOOL_KIND"]      = $row["SCHOOL_KIND"];
                    $dataTemp[$cnt]["CURRICULUM_CD"]    = $row["CURRICULUM_CD"];
                }
                $dataTemp[$cnt]["SUBCLASSCD"]       = $row["SUBCLASSCD"];
                $dataTemp[$cnt]["SUBCLASSNAME"]     = $row["SUBCLASSNAME"];
                $dataTemp[$cnt]["VALUATION"]        = ($row["YEAR"] == "9999") ? "" : $row["VALUATION"];
                $dataTemp[$cnt]["GET_CREDIT"]       = $row["GET_CREDIT"];
                $dataTemp[$cnt]["ADD_CREDIT"]       = ($row["YEAR"] == "9999") ? "" : $row["ADD_CREDIT"];
                $dataTemp[$cnt]["COMP_CREDIT"]      = $row["COMP_CREDIT"];

                $cnt++;
            }
        }
        $result->free();

        //ヘッダー
        $header = array("GRADE"         => "学年",
                        "HR_CLASS"      => "組",
                        "ATTENDNO"      => "出席番号",
                        "NAME_SHOW"     => "氏名",
                        "SCHREGNO"      => "学籍番号"
                        );

        if ($this->field["OUTPUT"] == "1") {
            $headCnt = 0;
            foreach ($tempCnt as $key => $val) {
                $headCnt = $headCnt < $val ? $val : $headCnt;
            }
            for ($idx = 0; $idx < $headCnt; $idx++) {
                $header["YEAR".$idx]          = "年度";
                $header["SUBCLASSNAME".$idx]  = "科目名";
                $header["VALUATION".$idx]     = "評定";
                $header["GET_CREDIT".$idx]    = "修得単位数";
                $header["ADD_CREDIT".$idx]    = "増加単位数";
                $header["COMP_CREDIT".$idx]   = "履修単位数";
            }
            $header["ABROAD_CREDITS"]   = "留学単位数";
        } else {
            $header["YEAR"]             = "年度";
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                $header["CLASSCD"]          = "教科コード";
                $header["SCHOOL_KIND"]      = "学校種別";
                $header["CURRICULUM_CD"]    = "教育課程コード";
            }
            $header["SUBCLASSCD"]       = "科目コード";
            $header["SUBCLASSNAME"]     = "科目名";
            $header["VALUATION"]        = "評定";
            $header["GET_CREDIT"]       = "修得単位数";
            $header["ADD_CREDIT"]       = "増加単位数";
            $header["COMP_CREDIT"]      = "履修単位数";
        }
        $contents = implode($header, ",") ."\n";

        //データ
        $data = false;
        foreach ($dataTemp as $key => $val) {
            //データをセット
            $contents .= implode($val, ",") ;

            if ($this->field["OUTPUT"] == "1") {
                //留学単位数の列まで空ける
                $karagyou = $headCnt - $tempCnt[$key];
                if ($karagyou) {
                    for ($idx = 0; $idx < $karagyou; $idx++) {
                        $contents .= ",,,,,,";
                    }
                }
                $contents .= ",".$dataTemp2[$key]["ABROAD_CREDITS"];
            }
            $contents .= "\n";
            $data = true;
        }

        //ファイル名用学年取得
        $query = knje152Query::getGrade($this, $this->field["GRADE"]);
        $grade = $db->getOne($query);

        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($data) {
            $nendo = CTRL_YEAR."年度";
            $filename = sprintf("%s.csv", $nendo.$grade."科目別成績詳細情報");
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
