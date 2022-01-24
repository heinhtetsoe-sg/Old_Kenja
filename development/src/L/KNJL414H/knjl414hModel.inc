<?php

require_once('for_php7.php');

//定数定義
class knjl414hModel extends Model
{
    public $field = array();
    public $cmd;
    public $year;
    public $applicantdiv;
    public $radio;
    public $s_receptOrg;
    public $s_receptno;
    public $examnos;
    public $jdgLine;
    public $hopeInf;

    //メンバ変数の初期化
    public function knjl414hModel()
    {
    }
    public function init()
    {
        $this->cmd  = VARS::request("cmd");
        if (VARS::get("PROGRAMID")) {
            $this->year = CTRL_YEAR + 1;
            $this->applicantdiv = "";
            $this->radio = "1";
            $this->s_receptOrg = "";
            $this->s_receptno = "";
            $this->examnos = array();
        } else {
            $this->applicantdiv = VARS::post("APPLICANTDIV");
            $this->radio = VARS::post("RADIO");
            $this->s_receptOrg = VARS::post("S_RECEPTNO");
            $this->s_receptno = $this->s_receptOrg != "" ? sprintf("%07s", VARS::post("S_RECEPTNO")) : "";
            $this->examnos = explode(',', VARS::post("examnos"));
        }
        if ($this->cmd == 'back' || $this->cmd == 'next') {
            $db = Query::dbCheckOut();
            $i = 0;
            $examnoList = array();
            $examnoIdx = null;
            $exno = $db->getOne(knjl414hQuery::getExamNo($this, $this->s_receptno));
            $result = $db->query(knjl414hQuery::getExamnoList($this));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $examnoList[] = $row['EXAMNO'];
                if (is_null($examnoIdx) && $row['EXAMNO'] >= $exno) {
                    $examnoIdx = $i;
                }
                $i++;
            }
            if (get_count($examnoList) == 0) {
                //何もしない
            } elseif ($this->cmd == 'back') {
                if (is_null($examnoIdx)) {
                    $examnoIdx = $i - 1;
                }
                $examnoIdx -= 50;
                if ($examnoIdx < 0) {
                    $examnoIdx = 0;
                }
                $rcptWk = $db->getOne(knjl414hQuery::getReceptNo($this, $examnoList[$examnoIdx]));
                if ($this->s_receptno != $rcptWk) {
                    $this->s_receptOrg = $rcptWk;
                    $this->s_receptno = $rcptWk;
                }
            } elseif ($this->cmd == 'next') {
                $examnoIdx += 50;
                if ($examnoIdx < get_count($examnoList)) {
                    $rcptWk = $db->getOne(knjl414hQuery::getReceptNo($this, $examnoList[$examnoIdx]));
                    if ($this->s_receptno != $rcptWk) {
                        $this->s_receptOrg = $rcptWk;
                        $this->s_receptno = $rcptWk;
                    }
                }
            }
            Query::dbCheckIn($db);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    //データ取得
    public function getPostData()
    {
        $SGZ = "01";
        $DISCOVER = "02";

        $testRadJConf = "1";
        $testRadMock = "2";
        $testRadOPT = "3";

        $db = Query::dbCheckOut();
        if ($this->radio == $testRadJConf) {
            //判定(10,11)  //出願区分、コース
            $this->jdgLine = array();
            $query = knjl414hQuery::getNaisinKijyun($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $this->jdgLine[] = $row;
            }
            $result->free();
            if ($this->applicantdiv == "2") {
                $query = knjl414hQuery::getCommitteeFreq($this);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $this->comFreq[$row["EXAMNO"]] = $row;
                }
                $result->free();
            }
        } elseif ($this->radio == $testRadMock) {
            if ($this->applicantdiv == "2") {
                //設定は、特進A、特進B、進学カルティベートの順で、専願/併願セットで登録。
                $this->jdgLine = array(
                                       "1"=>array(array(64,66),array(55,57),array(43,45)),  //ITUKI_KYOTO
                                       "2"=>array(array(62,64),array(53,55),array(41,43)),  //O_SINKEN
                                       "3"=>array(array(47,48),array(40,41),array(30,31)),  //SEIKI
                                       "4"=>array(array(60,62),array(50,52),array(38,40)),  //EIKO
                                       "5"=>array(array(59,61),array(51,53),array(39,41)),  //OKAMOTO
                                       "6"=>array(array(41,43),array(31,33),array(26,28)),  //SUNDAI
                                       "7"=>array(array(61,63),array(52,54),array(40,42)),  //ITUKI_KOUKOU
                                       "8"=>array(array(61,63),array(52,54),array(40,42))   //KAISEI
                                       );
                $query = knjl414hQuery::getList($this, true);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $this->ListInfo[$row["EXAMNO"]] = $row;
                }
                $result->free();
            } else {
                $this->jdgLine = array(
                                       $SGZ=>array(50,48,50,40,33,41,43),
                                       $DISCOVER=>array(45,42,44,35,30,38,40)
                                      );
                $this->jdgExemption = array(
                                            "FULL"=>array(53,48.50,43,35,43,45),
                                            "HALF"=>array(50,45,48,40,33,41,43)
                                           );
            }
        } elseif ($this->radio == $testRadOPT) {
            //登録直前に実施
            $this->l101List = array();
            $query = knjl414hQuery::getSettingMstl101($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $this->l101List[] = $row;
            }
            $result->free();
        }
        $this->hopeInf = array();
        $query = knjl414hQuery::getHopeInfo($this);
        $result = $db->query($query);
        $hcnt = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!is_array($this->hopeInf[$row["EXAMNO"]])) {
                $this->hopeInf[$row["EXAMNO"]] = array();
            }
            if ($row["HOPE_COURSE1"] != "") {
                $hcnt++;
                $this->hopeInf[$row["EXAMNO"]][] = $row["HOPE_COURSE1"]."-".$row["HOPE_TESTDIV1"];
            }
            if ($row["HOPE_COURSE2"] != "") {
                $hcnt++;
                $this->hopeInf[$row["EXAMNO"]][] = $row["HOPE_COURSE2"]."-".$row["HOPE_TESTDIV2"];
            }
            if ($row["HOPE_COURSE3"] != "") {
                $hcnt++;
                $this->hopeInf[$row["EXAMNO"]][] = $row["HOPE_COURSE3"]."-".$row["HOPE_TESTDIV3"];
            }
        }
        //sort($this->hopeInf);
        $result->free();
        Query::dbCheckIn($db);

        $data= array("CD2REMARK1"   =>  VARS::post("CD2REMARK1"),
                     "CD2REMARK2"   =>  VARS::post("CD2REMARK2"),
                     "CD2REMARK3"   =>  VARS::post("CD2REMARK3"),
                     "CD2REMARK4"   =>  VARS::post("CD2REMARK4"),
                     "CD2REMARK7"   =>  VARS::post("CD2REMARK7"),
                     "CD3REMARK1"   =>  VARS::post("CD3REMARK1"),
                     "CD3REMARK2"   =>  VARS::post("CD3REMARK2"),
                     "CD4REMARK1"   =>  VARS::post("CD4REMARK1"),
                     "CD4REMARK2"   =>  VARS::post("CD4REMARK2"),
                     "CD4REMARK3"   =>  VARS::post("CD4REMARK3"),
                     "CD4REMARK4"   =>  VARS::post("CD4REMARK4")
                     );
        //固定値
        $SUISEN = "1";
        $SENGAN = "2";
        $HEIGAN = "3";
        $FULLSUPORT = "1";
        $HALFSUPORT = "2";
        $PER90 = "1";
        $PER80 = "2";
        $PER70 = "3";
        $EXCDTOKUA = "02";  //特進Aコース
        $data2 = array();
        for ($eIdx = 0; $eIdx < get_count($this->examnos); $eIdx++) {
            $examno = $this->examnos[$eIdx];
            if ($this->radio == $testRadJConf) {
                //このルートは高校のみ処理
                $data2[$examno]['CD2REMARK1'] = $data['CD2REMARK1'][$eIdx];
                $data2[$examno]['CD2REMARK2'] = $data['CD2REMARK2'][$eIdx];
                $data2[$examno]['CD2REMARK3'] = $data['CD2REMARK3'][$eIdx];
                $data2[$examno]['CD2REMARK4'] = $data['CD2REMARK4'][$eIdx];
                $data2[$examno]['CD2REMARK7'] = $data['CD2REMARK7'][$eIdx];
                $data2[$examno]['CD5REMARK1'] = null;
                $data2[$examno]['CD5REMARK2'] = null;
                $data2[$examno]['CD5REMARK3'] = null;
                $data2[$examno]['CD5REMARK5'] = null;
                $data2[$examno]['CD5REMARK6'] = null;
                $useComFreq = $this->comFreq[$examno];
                $lineOverArry = array();
                $lineOverArry2 = array();
                $hopeInf = $this->hopeInf[$examno];
                $KARICD = "-";
                if (get_count($hopeInf) > 0) {
                    foreach ($hopeInf as $cStr) {
                        $cutsplt = explode('-', $cStr);
                        foreach ($this->jdgLine as $rWk) {
                            if ($rWk["EXAMCOURSECD"]."-".$rWk["SHDIV"] != $cStr) {
                                continue;
                            }
                            if ($rWk["SHDIV"] == $SUISEN && $rWk["BORDER1"] != "" && $data2[$examno]['CD2REMARK1'] != "") {
                                if ($rWk["BORDER1"] <= $data2[$examno]['CD2REMARK1']) {
                                    $lineOverArry[$cStr] = $HALFSUPPORT;
                                    break;
                                }
                            }
                            $katen = 0;
                            if (!is_null($useComFreq) && get_count($useComFreq) > 0) {
                                if ($useComFreq["BDSEQ5R2"] == "1") {  //生徒会
                                    $katen++;
                                } elseif ($useComFreq["BDSEQ5R3"] == "1" && $useComFreq["BDSEQ5R4"] != "" && $useComFreq["BDSEQ5R5"] != "") {  //資格
                                    $katen++;
                                }
                            }
                            if (($rWk["BORDER2"] != "" && $rWk["BORDER2"] <= $data2[$examno]['CD2REMARK2']) || ($rWk["BORDER3"] != "" && $rWk["BORDER3"] <= $data2[$examno]['CD2REMARK3'] + $katen)) {
                                $lineOverArry[$cStr] = $KARICD;
                                break;
                            }
                            if ($rWk["SHDIV"] == $SUISEN && $data2[$examno]['CD2REMARK4'] == "1" && !is_null($data2[$examno]['CD2REMARK3']) && !is_null($rWk["BORDER4"])) {  //強化クラブ(推薦基準)を判定
                                if ($rWk["BORDER4"] <= $data2[$examno]['CD2REMARK3']) {
                                    $lineOverArry2[$cStr] = $KARICD;
                                }
                            }
                            if ($rWk["EXAMCOURSECD"] == $EXCDTOKUA) {
                                if (!is_null($rWk["BORDER5"]) && !is_null($rWk["BORDER6"])) {  //両方入力必須
                                    if ($rWk["BORDER5"] <= $data2[$examno]['CD2REMARK1'] || $rWk["BORDER6"] <= $data2[$examno]['CD2REMARK2']) {  //どっちか満たせばOK
                                        if ($data2[$examno]['CD2REMARK4'] == "1") {
                                            if ($lineOverArry2[$cStr] == $KARICD) {
                                                $lineOverArry2[$cStr] = $FULLSUPORT;
                                            }
                                        } else {
                                            if ($lineOverArry[$cStr] == $KARICD) {
                                                $lineOverArry[$cStr] = $FULLSUPORT;
                                            }
                                        }
                                    }
                                }
                                if (!is_null($rWk["BORDER7"]) && !is_null($rWk["BORDER8"])) {  //両方入力必須
                                    if ($rWk["BORDER7"] <= $data2[$examno]['CD2REMARK1'] || $rWk["BORDER8"] <= $data2[$examno]['CD2REMARK2']) {  //どっちか満たせばOK
                                        if ($data2[$examno]['CD2REMARK4'] == "1") {
                                            if ($lineOverArry2[$cStr] == $KARICD) {
                                                $lineOverArry2[$cStr] = $FULLSUPORT;
                                            }
                                        } else {
                                            if ($lineOverArry[$cStr] == $KARICD) {  //合格していたら設定
                                                $lineOverArry[$cStr] = $HALFSUPORT;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (is_array($lineOverArry) && get_count($lineOverArry) > 0) {
                        ksort($lineOverArry);
                        foreach ($lineOverArry as $setKey => $sup) {
                            $cutKey = explode('-', $setKey);
                            $data2[$examno]['CD5REMARK1'] = $cutKey[0];
                            $data2[$examno]['CD5REMARK2'] = $cutKey[1];
                            $data2[$examno]['CD5REMARK3'] = $sup == $KARICD ? "" : $sup;
                            break;
                        }
                    }
                    if (is_array($lineOverArry2) && get_count($lineOverArry2) > 0) {
                        ksort($lineOverArry2);
                        foreach ($lineOverArry2 as $setKey => $sup) {
                            $cutKey = explode('-', $setKey);
                            $data2[$examno]['CD5REMARK5'] = $cutKey[0];
                            $data2[$examno]['CD5REMARK6'] = $cutKey[1];
                            //強化クラブは特進Aではないので奨学金は発生しない。
                            break;
                        }
                    }
                }
            } elseif ($this->radio == $testRadMock) {
                $data2[$examno]['CD3REMARK1'] = $data['CD3REMARK1'][$eIdx];
                $data2[$examno]['CD3REMARK2'] = $data['CD3REMARK2'][$eIdx];
                $data2[$examno]['CD3REMARK10'] = null;
                $data2[$examno]['CD3REMARK11'] = null;
                $data2[$examno]['CD3REMARK12'] = null;
                $lineOverArry = array();
                $hopeInf = $this->hopeInf[$examno];
                //※内部資料に記載の模擬テストの表を元に作成。
                //処理として、希望するコース/出願区分毎に判定を行い、該当する閾値を超えていたらコース/出願区分を登録対象とする(arrayに取りためる)。
                //取りためたコース&出願区分をソートして、先頭のデータを登録する。
                if ($data2[$examno]['CD3REMARK1'] != "" && get_count($hopeInf) > 0) {
                    if ($this->applicantdiv == "2") {
                        $chkArry = $this->jdgLine[$data2[$examno]['CD3REMARK1']];
                        foreach ($hopeInf as $cStr) {
                            $cutsplt = explode('-', $cStr);
                            if (intval($cutsplt[0]) <= 1 || intval($cutsplt[0])-2 > get_count($chkArry) || $cutsplt[1] == $SUISEN) {  //模試判定の表に推薦が無いので、推薦は判定対象外。特進A特別推薦も対象外。
                                continue;
                            }
                            foreach ($chkArry[intval($cutsplt[0])-2] as $cVal) {
                                if ($cutsplt[1] == $HEIGAN && $cVal[1] <= $data2[$examno]['CD3REMARK2']) {
                                    $lineOverArry[] = $cStr;
                                } elseif ($cutsplt[1] == $SENGAN && $cVal[0] <= $data2[$examno]['CD3REMARK2']) {
                                    $lineOverArry[] = $cStr;
                                }
                            }
                        }
                        if (get_count($lineOverArry) > 0) {
                            //受験コード&出願区分をソートして、先頭を取得。
                            sort($lineOverArry);
                            foreach ($lineOverArry as $setKey) {
                                $setSplt = explode('-', $setKey);
                                $data2[$examno]['CD3REMARK10'] = $setSplt[0];  //受験コース
                                $data2[$examno]['CD3REMARK11'] = $setSplt[1];  //出願区分
                                //特進Aコース併願の偏差値以上を半額奨学生とする
                                if ($chkArry[0][1] <= $data2[$examno]['CD3REMARK2']) {
                                    $data2[$examno]['CD3REMARK12'] = $HALFSUPORT;
                                }
                                break;  //先頭だけ取得。
                            }
                        }
                    } else {
                        //希望コースがある時のみ設定(変更しやすいように、希望コースの取得順にチェックして取り溜めして、後でコードでソートする)
                        if (get_count($hopeInf) > 0) {
                            foreach ($hopeInf as $cStr) {
                                $cutsplt = explode('-', $cStr);
                                if ($this->jdgLine[$cutsplt[0]][$data2[$examno]['CD3REMARK1']] <= $data2[$examno]['CD3REMARK2']) {
                                    $lineOverArry[] = $cStr;
                                }
                            }
                            if (get_count($lineOverArry) > 0) {
                                //受験コード&出願区分をソートして、先頭を取得。
                                sort($lineOverArry);
                                foreach ($lineOverArry as $setKey) {
                                    $setSplt = explode('-', $setKey);
                                    $data2[$examno]['CD3REMARK10'] = $setSplt[0];  //受験コース
                                    $data2[$examno]['CD3REMARK11'] = $setSplt[1];  //出願区分
                                    break;  //先頭だけ取得。
                                }
                            }
                        }
                        if ($this->jdgExemption["FULL"][$data2[$examno]['CD3REMARK1']] <= $data2[$examno]['CD3REMARK2']) {
                            $data2[$examno]['CD3REMARK12'] = $FULLSUPORT;
                        } elseif ($this->jdgExemption["HALF"][$data2[$examno]['CD3REMARK1']] <= $data2[$examno]['CD3REMARK2']) {
                            $data2[$examno]['CD3REMARK12'] = $HALFSUPORT;
                        }
                    }
                }
            } elseif ($this->radio == $testRadOPT) {
                //このルートは中学のみ。
                $data2[$examno]['CD4REMARK1'] = $data['CD4REMARK1'][$eIdx];
                $data2[$examno]['CD4REMARK2'] = $data['CD4REMARK2'][$eIdx];
                $data2[$examno]['CD4REMARK3'] = $data['CD4REMARK3'][$eIdx];
                $data2[$examno]['CD4REMARK4'] = null;
                $data2[$examno]['CD4REMARK10'] = null;
                $data2[$examno]['CD4REMARK11'] = null;
                $data2[$examno]['CD4REMARK12'] = null;
                //最大入力値を求めてL101テーブルと突き合わせて判定コードを設定
                $maxvalue = $data['CD4REMARK1'][$eIdx];
                if ($data['CD4REMARK2'][$eIdx] > $maxvalue) {
                    $maxvalue = $data['CD4REMARK2'][$eIdx];
                }
                if ($data['CD4REMARK3'][$eIdx] > $maxvalue) {
                    $maxvalue = $data['CD4REMARK3'][$eIdx];
                }
                $hopeInf = $this->hopeInf[$examno];
                //※内部資料に記載の模擬テストの表を元に作成。
                //処理として、希望するコース/出願区分毎に判定を行い、該当する閾値を超えていたらコース/出願区分と"%"を登録対象とする(arrayに取りためる)。
                //取りためたarrayをコース&出願区分でソートして、先頭のデータを登録する。
                $lineOverArry = array();
                if (get_count($hopeInf) > 0) {
                    foreach ($hopeInf as $cStr) {
                        $cutsplt = explode('-', $cStr);
                        for ($i = 0; $i < get_count($this->l101List); $i++) {
                            if ($this->l101List[$i]['NAMESPARE1'] != $cutsplt[0]) {
                                continue;
                            }
                            //受験コード&出願区分をキー、%を値として保持。
                            if ($maxvalue >= $this->l101List[$i]['NAME2']) {
                                $lineOverArry[$cStr] = $this->l101List[$i]['VALUE'];
                            }
                        }
                    }
                    if (get_count($lineOverArry) > 0) {
                        //受験コード&出願区分でソートして、先頭を取得。
                        ksort($lineOverArry);
                        foreach ($lineOverArry as $setKey => $l101Val) {
                            $setSplt = explode('-', $setKey);
                            $data2[$examno]['CD4REMARK10'] = $setSplt[0];  //受験コース
                            $data2[$examno]['CD4REMARK11'] = $setSplt[1];  //出願区分
                            $data2[$examno]['CD4REMARK4'] = $l101Val;  //%
                            break;  //先頭だけ取得。
                        }
                    }
                    //設定値に応じてコース、奨学生金額(全額/半額)を確定する。
                    if ($data2[$examno]['CD4REMARK4'] == $PER90) {
                        $data2[$examno]['CD4REMARK12'] = $FULLSUPORT;
                    } elseif ($data2[$examno]['CD4REMARK4'] == $PER80) {
                        $data2[$examno]['CD4REMARK12'] = $HALFSUPORT;
                    }
                }
            }
        }
        return $data2;
    }

    //入力項目チェック
    public function validateInput($kind)
    {
        if (is_null($this->applicantdiv)) {
            $this->setWarning("MSG310", "入試制度".$this->applicantdiv);
        }
        if (is_null($this->radio)) {
            $this->setWarning("MSG310", "内申/模試/OPTラジオボタン".$this->radio);
        }
        $data= array("CD2REMARK1"   =>  VARS::post("CD2REMARK1"),
                     "CD2REMARK2"   =>  VARS::post("CD2REMARK2"),
                     "CD2REMARK3"   =>  VARS::post("CD2REMARK3"),
                     "CD2REMARK4"   =>  VARS::post("CD2REMARK4"),
                     "CD2REMARK7"   =>  VARS::post("CD2REMARK7"),
                     "CD3REMARK1"   =>  VARS::post("CD3REMARK1"),
                     "CD3REMARK2"   =>  VARS::post("CD3REMARK2"),
                     "CD4REMARK1"   =>  VARS::post("CD4REMARK1"),
                     "CD4REMARK2"   =>  VARS::post("CD4REMARK2"),
                     "CD4REMARK3"   =>  VARS::post("CD4REMARK3"),
                     "CD4REMARK4"   =>  VARS::post("CD4REMARK4")
                     );
        for ($eIdx = 0; $eIdx < get_count($this->examnos); $eIdx++) {
            if ($this->radio == '1') {
                if (!$this->chkNumStrIgNull($data['CD2REMARK1'][$eIdx])) {
                    $this->setWarning("MSG907", "内諾5科(中学3年間)".$data['CD2REMARK1'][$eIdx]);
                    return false;
                }
                if (!$this->chkNumStrIgNull($data['CD2REMARK2'][$eIdx])) {
                    $this->setWarning("MSG907", "内諾5科(中学3年12月末)".$data['CD2REMARK2'][$eIdx]);
                    return false;
                }
                if (!$this->chkNumStrIgNull($data['CD2REMARK3'][$eIdx])) {
                    $this->setWarning("MSG907", "内諾9科(中学3年12月末)".$data['CD2REMARK3'][$eIdx]);
                    return false;
                }
            } elseif ($this->radio == '2') {
                if (is_null($data['CD3REMARK1'][$eIdx])) {
                    $this->setWarning("MSG310", "模試名".$data['CD3REMARK1'][$eIdx]);
                    return false;
                }
                if (!$this->chkNumStrIgNull($data['CD3REMARK2'][$eIdx])) {
                    $this->setWarning("MSG907", "模試結果".$data['CD3REMARK2'][$eIdx]);
                    return false;
                }
            } elseif ($this->radio == '3') {
                if (!$this->chkNumStrIgNull($data['CD4REMARK1'][$eIdx])) {
                    $this->setWarning("MSG907", "OPT結果1".$data['CD4REMARK1'][$eIdx]);
                    return false;
                }
                if (!$this->chkNumStrIgNull($data['CD4REMARK2'][$eIdx])) {
                    $this->setWarning("MSG907", "OPT結果2".$data['CD4REMARK2'][$eIdx]);
                    return false;
                }
                if (!$this->chkNumStrIgNull($data['CD4REMARK3'][$eIdx])) {
                    $this->setWarning("MSG907", "OPT結果3".$data['CD4REMARK3'][$eIdx]);
                    return false;
                }
            }
        }
        return true;
    }

    public function chkNumStrIgNull($cStr)
    {
        if ($cStr != "") {
            if (preg_match('/^[0-9]+$/', $cStr) != 1) {
                return false;
            }
        }
        return true;
    }

    //変更処理
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")) {
            $result = knjl414hQuery::getUpdateQuery($this, $this->getPostData());
            if (!isset($this->warning)) {
                $this->setMessage("MSG201");
            }
        }
    }

    //削除処理
    public function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")) {
            $result = knjl414hQuery::getDeleteQuery($this, $this->getPostData());
            if (!isset($this->warning)) {
                $this->setMessage("MSG201");
            }
        }
        return;
    }
    //変数をクリアします
    public function clean()
    {
    }
}
