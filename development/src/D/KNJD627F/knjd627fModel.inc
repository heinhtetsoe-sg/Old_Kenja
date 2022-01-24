<?php

require_once('for_php7.php');

class knjd627fModel extends Model
{
    public $programID;
    public $cmd;
    public $year;
    public $semester;
    public $grade;
    public $proficiencyDiv; //試験区分
    public $proficiencyCd;  //試験コード
    public $printTarget;
    public $DANGERCREDITLINE = 74;           //単位合否基準点
    public $DANGERSOUTEN_ONEYEARLINE = 1800; //年次別合否基準点
    public $DANGERSOUTENLINE = 5400;         //推薦合否基準点
    public $GET_TESTCD = "9-990008";//単位取得にかかる科目テストコード（学期N＋テスト種別NN＋テスト項目NNN＋テスト区分N）
    public $CLSFULL = "ZZ"; //総計項目に対する仮の科目コード
    public $NotIncludeSubClss = array(  //対象外とする合計科目コード
        '333333',
        '555555',
        '999999',
        '99999A',
        '99999B'
    );
    public $SEMEALL = "9";  //学年

    //条件不十分事由ビット値
    public $FUSOKU_FAIL_BIT  = 0x200;
    public $ENGLISH_FAIL_BIT = 0x100;
    public $SOUTEN_NULL_BIT  = 0x080;
    public $D1_NULL_BIT      = 0x040;
    public $D2_NULL_BIT      = 0x020;
    public $D3_NULL_BIT      = 0x010;
    public $SOUTEN_FAIL_BIT  = 0x008;
    public $D1_FAIL_BIT      = 0x004;
    public $D2_FAIL_BIT      = 0x002;
    public $D3_FAIL_BIT      = 0x001;

    public function init()
    {
        $this->year           = CTRL_YEAR;
        $this->cmd            = VARS::request("cmd");
        $this->semester       = VARS::post("SEMESTER");
        $this->grade          = VARS::post("HID_GRADE");
        $this->proficiencyDiv = VARS::post("PROFICIENCYDIV");
        $this->proficiencyCd  = VARS::post("PROFICIENCYCD");
        $this->printTarget    = VARS::post("PRINT_TARGET_KIND");

        if (isset($this->printTarget) == false) {
            $this->printTarget = "1";
        }

        if (isset($this->semester) == false) {
            $this->semester = CTRL_SEMESTER;
        }

        $this->selectSchoolKind = VARS::get("URL_SCHOOLKIND");
        $this->getPropertiesAll(); //プロパティファイル読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //学校種別の指定有無を取得
    public function isIndicateSchoolKind()
    {
        return ($this->selectSchoolKind != "");
    }

    //卒業条件不備判定
    // 戻り値　TRUE：卒業条件不備／FALSE：卒業条件を満たしている
    private function chkDangerLine($souten)
    {
        if ($souten["CLASSCD"] == $this->CLSFULL) { //総計項目
            if ((int)$souten["T_CREDITS"] < $this->DANGERCREDITLINE) {
                return true; //単位取得不十分
            }
        } else {
            if ($souten["CHK_STAR"] == "1") {
                return true; //対象科目単位取得不十分
            }
        }

        return false;
    }

    //推薦条件不備対象判定
    //戻り値：0x0000 出力対象外　0x**** 何らかの事由により出力対象（推薦条件不備）
    private function judgeSuisen($souten)
    {
        $retVal = 0x0000;
        if (is_null($souten)) {
            //出力対象外扱い
            return $retVal;
        }

        if (isset($souten["SOUTEN"]) == false) {
            $retVal |= $this->SOUTEN_NULL_BIT;
        } elseif ((int)$souten["SOUTEN"] < $this->DANGERSOUTENLINE) {
            $retVal |= $this->SOUTEN_FAIL_BIT;
        }

        if (($retVal & $this->SOUTEN_NULL_BIT) != $this->SOUTEN_NULL_BIT) {
            if ((int)$souten["FUSOKUTEN"] < 0) {
                $retVal |= $this->FUSOKU_FAIL_BIT;
            }
        }

        if (isset($souten["RANK"]) && $souten["JUDGE"] != "1") {
            $retVal |= $this->ENGLISH_FAIL_BIT;
        }

        return retVal;
    }

    //CSVファイルダウンロード
    public function outputCSV()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //卒業年次学生情報を取得
        $students = array();
        $query = knjd627fQuery::getLastGradeStudentsInYear($this);
        $result = $db->query($query);
        while (is_null($result) == false && $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $students[] = $row;
        }
        $result->free();
        if (get_count($students) == false) {
            //DB切断
            Query::dbCheckIn($db);

            $this->setWarning("MSG303");
            return false;
        }

        $filename = ""; //ダウンロードファイル名
        $header = null;

        //クエリ生成とスコア取得
        switch ($this->printTarget) {
            case "1": //卒業条件不備者一覧
                $query = knjd627fQuery::getCreditSouten($this);
                $filename = $this->year."年度卒業条件不備者一覧.csv";
                break;

            case "2": //推薦条件不備者一覧
                $query = knjd627fQuery::getSouten($this, $subclses);
                $filename = $this->year."推薦条件不備者一覧.csv";
                break;
        }

        $list = array();
        $result = $db->query($query);
        while (is_null($result) == false && $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $list[] = $row;
        }
        $result->free();

        if (get_count($list) < 1) {
            //DB切断
            Query::dbCheckIn($db);
            $this->setWarning("MSG303");
            return false;
        }

        // 名称等を取得
        switch ($this->printTarget) {
            case "1": //卒業条件不備者一覧
                // 科目一覧を取得
                $subclsCdMap = array(); //未履修科目
                $query = knjd627fQuery::getClassCodes($this);
                $result = $db->query($query);
                while (is_null($result) == false && $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $subclsCdMap[] = $row;
                }
                $result->free();
                break;

            case "2": //推薦条件不備者一覧
                break;
        }

        //DB切断
        Query::dbCheckIn($db);

        //集計
        $summary = array();
        switch ($this->printTarget) {
            case "1": //卒業条件不備者一覧
                $header = array(
                    "NO"    => "番号" ,
                    "NAME"  => "氏名",
                    "CNAME" => "コース名",
                    "TOTAL" => "習得単位"
                    //定義順＝出力順となるため後で定義を追加
                    //"NotPassG01" => "1学年未収得科目",
                    //"NotPassG02" => "2学年未収得科目",
                    //"NotPassG03" => "3学年未収得科目"
                );
                while ($student = array_shift($students)) {
                    $keyRegNo = "REGNO_" . $student["SCHREGNO"];
                    for ($idx =0; $idx < get_count($list);) {
                        if ($list[$idx]["SCHREGNO"] == $student["SCHREGNO"]) {
                            if (isset($summary["{$keyRegNo}"]) == false) {
                                $summary["{$keyRegNo}"] = array(
                                    "NO"      => $student["HR_CLASS_NAME1"] . $student["ATTENDNO"] ,
                                    "NAME"    => $student["NAME"],
                                    "CNAME"   => $student["COURSECODEABBV1"],
                                    "IsMatch" => false
                                );
                            }

                            if ($list[$idx]["CLASSCD"] == $this->CLSFULL) {
                                $summary["{$keyRegNo}"]["TOTAL"] = $list[$idx]["T_CREDITS"];
                                if ((int)$list[$idx]["T_CREDITS"] < $this->DANGERCREDITLINE) {
                                    $summary["{$keyRegNo}"]["TOTAL"] .= "*";
                                }
                                $summary["{$keyRegNo}"]["NotPassG01"] = $list[$idx]["NPREMARK1"];
                                $summary["{$keyRegNo}"]["NotPassG02"] = $list[$idx]["NPREMARK2"];
                                $summary["{$keyRegNo}"]["NotPassG03"] = $list[$idx]["NPREMARK3"];
                            } else {
                                foreach ($subclsCdMap as $subcls) {
                                    if ($subcls["CLASSCD"] == $list[$idx]["CLASSCD"]) {
                                        $subclsKey = "CLS_" . $subcls["CLASSCD"];
                                        if (array_key_exists($subclsKey, $header) == false) {
                                            $header[$subclsKey] = $subcls["CLASSABBV"];
                                        }
                                        $summary["{$keyRegNo}"][$subclsKey] = $list[$idx]["T_CREDITS"];
                                        break;
                                    }
                                }
                            }

                            //出力判定（いずれかの出力条件を満たした場合出力対象となる）
                            if ($this->chkDangerLine($list[$idx])) {
                                $summary["{$keyRegNo}"]["IsMatch"] = true;
                            }

                            //マージし終えた要素を削除
                            array_splice($list, $idx, 1);
                            //次の要素を参照
                            continue;
                        }

                        //次の要素を参照
                        $idx++;
                    }
                } // End of loop :students

                //出力対象外レコード除外および未履修項目編集
                foreach ($summary as $regno => $rec) {
                    //出力対象外レコードを削除
                    if ($rec["IsMatch"] == false) {
                        array_splice($summary, $regno, 1);
                        continue;
                    }
                    //未履修項目編集
                    $summary[$regno]["NotPassG01"] = str_replace(",", " ", $summary[$regno]["NotPassG01"]);
                    $summary[$regno]["NotPassG02"] = str_replace(",", " ", $summary[$regno]["NotPassG02"]);
                    $summary[$regno]["NotPassG03"] = str_replace(",", " ", $summary[$regno]["NotPassG03"]);
                }

                //未履修項目ヘッダ定義を追加
                $header["NotPassG01"] = "1学年未収得科目";
                $header["NotPassG02"] = "2学年未収得科目";
                $header["NotPassG03"] = "3学年未収得科目";
                break;

            case "2": //推薦条件不備者一覧
                $header = array(
                    "NO"         => "番号" ,
                    "NAME"       => "氏名",
                    "CNAME"      => "コース名",
                    "TOTAL"      => "総計",
                    "LOSS"       => "不足点",
                    "G01"        => "１年",
                    "G02"        => "２年",
                    "G03"        => "３年",
                    "ENGLISH"    => "英語判定",
                    "NotPassG01" => "1学年未収得科目",
                    "NotPassG02" => "2学年未収得科目",
                    "NotPassG03" => "3学年未収得科目"
                );
                while ($student = array_shift($students)) {
                    $keyRegNo = "REGNO_" . $student["SCHREGNO"];
                    for ($idx =0; $idx < get_count($list);) {
                        if (   $list[$idx]["SCHREGNO"] == $student["SCHREGNO"]
                            && $this->judgeSuisen($list[$idx])
                        ) {
                            $summary["{$keyRegNo}"] = array(
                                "NO"         => $student["HR_CLASS_NAME1"] . $student["ATTENDNO"] ,
                                "NAME"       => $student["NAME"],
                                "CNAME"      => $student["COURSECODEABBV1"],
                                "TOTAL"      => $list[$idx]["SOUTEN"],
                                "LOSS"       => $list[$idx]["FUSOKUTEN"],
                                "G01"        => $list[$idx]["D1VAL"],
                                "G02"        => $list[$idx]["D2VAL"],
                                "G03"        => $list[$idx]["D3VAL"],
                                "ENGLISH"    => $list[$idx]["RANK"],
                                "NotPassG01" => str_replace(",", " ", $list[$idx]["NPREMARK1"]),
                                "NotPassG02" => str_replace(",", " ", $list[$idx]["NPREMARK2"]),
                                "NotPassG03" => str_replace(",", " ", $list[$idx]["NPREMARK3"])
                            );

                            //マージし終えた要素を削除
                            array_splice($list, $idx, 1);
                            //次の要素を参照
                            continue;
                        }

                        //次の要素を参照
                        $idx++;
                    }
                } // End of loop :students
                break;
        }

        //出力対象外レコード削除処理後の残件数を確認
        if (get_count($summary) < 1) {
            $this->setWarning("MSG303");
            return false;
        }

        //CSVデータ出力
        $contents = implode($header, ",")."\n";
        $isExistContents = false;
        foreach ($summary as $keyRegNo => $row) {
            $seperator = "";
            foreach ($header as $column => $columnName) {
                //帳票種別項目別フォーマット編集
                switch ($this->printTarget) {
                    case "1": //卒業条件不備者一覧
                        break;

                    case "2": //推薦条件不備者一覧
                        switch ($column) {
                            case "TOTAL":
                                $row[$column] = $row[$column] . " *";
                                break;
                            case "G01":
                            case "G02":
                            case "G03":
                                if ($row[$column] < $this->DANGERSOUTEN_ONEYEARLINE) {
                                    $row[$column] = "{$row[$column]} *";
                                }
                                break;
                        }
                        break;
                }

                //CSVデータ生成
                $contents .= $seperator . $row[$column];
                $seperator = ",";
                $isExistContents = true;
            }
            $contents .= "\n";
        }

        if ($isExistContents == false) {
            $this->setWarning("MSG303");
            return false;
        }

        //ファイルダウンロード
        common::downloadContents($contents, $filename);
        return true;
    }
}
