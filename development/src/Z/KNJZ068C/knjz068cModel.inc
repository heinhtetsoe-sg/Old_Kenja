<?php

require_once('for_php7.php');

class knjz068cModel extends Model {

    var $cmd;
    var $control_data = array();        //コントロールのデータを保持
    var $field;

    function knjz068cModel()
    {
        common::GetControlMaster_Fnc($this->control_data);
        $this->field = array();
    }

    function init()
    {
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        $this->cmd = VARS::request("cmd");

        $this->field["IBYEAR"] = VARS::post("IBYEAR");
        $this->field["IBGRADE"] = VARS::post("IBGRADE");
        $this->field["IBPRG_COURSE"] = VARS::post("IBPRG_COURSE");
        $this->field["IBSUBCLASSCD"] = VARS::post("IBSUBCLASSCD");

        //チェックボックス
        $this->updCheckBox = array();
        if (is_array($this->checkBoxName)) {
            foreach ($this->checkBoxName as $key => $val) {
                if (VARS::post($val)) {
                    $this->updCheckBox[] = VARS::post($val);
                }
            }
        }

    }

    /* 入力項目チェック */
    function validateInput()
    {
        return true;
    }

    /* 更新処理 */
    function &getUpdateModel()
    {
        if ($this->validateInput()){
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            $this->delete($db);

            foreach ($this->updCheckBox as $key => $val) {
                list($div1, $div2, $mark, $seq, $viewCd) = preg_split("/_/", $val);
                $this->insert($db, $div1, $div2, $mark, $seq, $viewCd);
            }

            $db->commit();
            Query::dbCheckIn($db);

            $this->setMessage("MSG201");
        }
        return;
    }

    /* 削除 */
    function &delete($db) {
        $query = knjz068cQuery::getDeleteSql($this);
        $db->query($query);
    }

    /* 新規 */
    function &insert($db, $div1, $div2, $mark, $seq, $viewCd)
    {
        $query = knjz068cQuery::getInsertSql($this, $div1, $div2, $mark, $seq, $viewCd);
        $db->query($query);
    }
    
    //CVSファイル
    //サーバ ⇒ クライアント
    function getDownloadCsvModel() {
        $db = Query::dbCheckOut();
        //ヘッダー情報を取得する
        $setLabel = "";
        $setConma = "";
        $setTitleKyoutu = "";
        $count=0;
        $lf = "\r\n";   //改行
        
        //対象データのリストを取得
        $query = knjz068cQuery::getIbUnitDatList($this);
        $result = $db->query($query);
        while ($listrow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //タイトル(横)
            $titleYoko = setTitleYokoModel($this, $db, $listrow);
            //タイトル(縦)
            $titleTate = setTitleTateModel($this, $db, $listrow);
            foreach ($titleTate as $tkey => $tVal) {
                foreach ($titleYoko as $yKey => $yVal) {
                    foreach ($yVal["VIEWCD"] as $key => $val) {
                        //ヘッダーセット
                        if ($setIbKey !== $tVal["IBYEAR"]."-".$tVal["IBGRADE"]."-".$tVal["IBCLASSCD"]."-".$tVal["IBPRG_COURSE"]."-".$tVal["IBCURRICULUM_CD"]."-".$tVal["IBSUBCLASSCD"]) {
                            //データが異なるため、最初は改行する
                            if ($count > 0) {
                                $contents .= $lf;
                            }
                            $setConma = ",,,,,,,,,";
                            $setTitleKyoutu = "年度,学年,IB科目,,,,IB科目名,評価規準／観点,評価規準名称,";
                            //タイトルデータを取得し、ループでまわす
                            $setSemester = "";
                            $setIbSubClass = "";
                            $setUnit = "";
                            $setViewcd = "";
                            $conma = "";
                            $query = knjz068cQuery::getIbUnit($this, $tVal);
                            $resultHeader = $db->query($query);
                            $viewNumber = 1;
                            while ($row = $resultHeader->fetchRow(DB_FETCHMODE_ASSOC)) {
                                //IBSEQが異なるとき
                                if ($checkSeq != $row["IBSEQ"]) {
                                    $setSemester    .= $conma.$row["SEMESTERNAME"];
                                    $setIbSubClass  .= $conma.$row["SUBCLASSNAME"];
                                    $setUnit        .= $conma."Unit".$row["IBSEQ"];
                                    $viewNumber = 1;
                                }
                                //観点をセット
                                //$setViewcd      .= $conma.$row["VIEWCD"];
                                if ($row["VIEWCD"]) {
                                    $setViewcd      .= $conma.$viewNumber.'.'.$row["VIEWNAME"];
                                } else {
                                    $setViewcd      .= $conma."";
                                }
                                $conma = ",";
                                //IBSEQが同じとき
                                if ($checkSeq == $row["IBSEQ"]) {
                                    $setSemester    .= $conma;
                                    $setIbSubClass  .= $conma;
                                    $setUnit        .= $conma;
                                }
                                $checkSeq = $row["IBSEQ"];
                                $viewNumber++;
                            }
                            $resultHeader->free();
                            $contents .= $setConma.$setSemester.$lf;
                            $contents .= $setConma.$setIbSubClass.$lf;
                            $contents .= $setConma.$setUnit.$lf;
                            $contents .= $setTitleKyoutu.$setViewcd;
                        }

                        //データセット
                        if ($setLabel !== $tVal["IBEVAL_DIV1"]."_".$tVal["IBEVAL_DIV2"]."_".$tVal["IBEVAL_MARK"]) {
                            $contents .= $lf;
                            $contents .= $tVal["IBYEAR"];
                            $contents .= ",".$tVal["IBGRADE"];
                            $contents .= ",".$tVal["IBCLASSCD"];
                            $contents .= ",".$tVal["IBPRG_COURSE"];
                            $contents .= ",".$tVal["IBCURRICULUM_CD"];
                            $contents .= ",".$tVal["IBSUBCLASSCD"];
                            $contents .= ",".$tVal["IBSUBCLASSNAME_ENG"];
                            $contents .= ",".$tVal["IBEVAL_MARK"];
                            $contents .= ",".$tVal["IBEVAL_NAME"];
                        }
                        $setIbKey = $tVal["IBYEAR"]."-".$tVal["IBGRADE"]."-".$tVal["IBCLASSCD"]."-".$tVal["IBPRG_COURSE"]."-".$tVal["IBCURRICULUM_CD"]."-".$tVal["IBSUBCLASSCD"];
                        $setIbSubclasscd = $tVal["IBCLASSCD"]."-".$tVal["IBPRG_COURSE"]."-".$tVal["IBCURRICULUM_CD"]."-".$tVal["IBSUBCLASSCD"];
                        $query = knjz068cQuery::getUnitPlanCnt($this, $tVal["IBEVAL_DIV1"], $tVal["IBEVAL_DIV2"], $tVal["IBEVAL_MARK"], $yVal["IBSEQ"], $val, $tVal["IBGRADE"], $setIbSubclasscd);
                        $checkCnt = $db->getOne($query);
                        if ($checkCnt > 0) {
                            $contents .= ",".'1';
                        } else {
                            $contents .= ",".'';
                        }
                        $setLabel = $tVal["IBEVAL_DIV1"]."_".$tVal["IBEVAL_DIV2"]."_".$tVal["IBEVAL_MARK"];
                        $count++;
                    }
                }
                $data = true;
            }
        }
        $result->free();
        
        Query::dbCheckIn($db);
        if ($data){
            $couse="IB評価基準_学習指導要領_観点別評価対応一覧(MYP)";
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename);
            return true;
        }
        else {
            $this->setWarning("MSG303");
            return false;
        }
        
    }
}

//タイトル(横)
function setTitleYokoModel($this, $db, $listrow) {
    $query = knjz068cQuery::getIbUnit($this, $listrow);
    $result = $db->query($query);

    $titleYoko = array();
    $titleYokoCnt = 0;
    $titleYokoBefSeq = "";

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($titleYokoBefSeq != "" && $titleYokoBefSeq != $row["IBSEQ"]) {
            $titleYokoCnt++;
            $viewCnt = 0;
        }
        $titleYokoBefSeq = $row["IBSEQ"];
        $titleYoko[$titleYokoCnt]["IBSEQ"] = $row["IBSEQ"];
        $titleYoko[$titleYokoCnt]["SEMESTER"] = $row["SEMESTERNAME"];
        $titleYoko[$titleYokoCnt]["SUBCLASS"] = $row["SUBCLASSNAME"];
        $titleYoko[$titleYokoCnt]["VIEWCD"][] = $row["VIEWCD"];
        $viewCnt++;
    }
    $result->free();

    return $titleYoko;
}

//タイトル(縦)
function setTitleTateModel($this, $db, $listrow) {
    $query = knjz068cQuery::getIbViewName($this, $listrow);
    $result = $db->query($query);

    $titleTate = array();
    $tateCnt = 0;
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $arg["HYOUKA"][] = $row["IBEVAL_MARK"];
        $titleTate[$tateCnt]["IBYEAR"] = $row["IBYEAR"];
        $titleTate[$tateCnt]["IBGRADE"] = $row["IBGRADE"];
        $titleTate[$tateCnt]["IBCLASSCD"] = $row["IBCLASSCD"];
        $titleTate[$tateCnt]["IBPRG_COURSE"] = $row["IBPRG_COURSE"];
        $titleTate[$tateCnt]["IBCURRICULUM_CD"] = $row["IBCURRICULUM_CD"];
        $titleTate[$tateCnt]["IBSUBCLASSCD"] = $row["IBSUBCLASSCD"];
        $titleTate[$tateCnt]["IBSUBCLASSNAME_ENG"] = $row["IBSUBCLASSNAME_ENG"];
        $titleTate[$tateCnt]["IBSUBCLASSCD_SET"] = $row["IBCLASSCD"].'-'.$row["IBPRG_COURSE"].'-'.$row["IBCURRICULUM_CD"].'-'.$row["IBSUBCLASSCD"];
        $titleTate[$tateCnt]["IBPERFECT"] = $row["IBPERFECT"];
        
        $titleTate[$tateCnt]["IBEVAL_MARK"] = $row["IBEVAL_MARK"];
        $titleTate[$tateCnt]["IBEVAL_NAME"] = $row["IBEVAL_NAME"];
        $titleTate[$tateCnt]["IBEVAL_DIV1"] = $row["IBEVAL_DIV1"];
        $titleTate[$tateCnt]["IBEVAL_DIV2"] = $row["IBEVAL_DIV2"];
        $tateCnt++;
    }
    $result->free();

    return $titleTate;
}


?>
