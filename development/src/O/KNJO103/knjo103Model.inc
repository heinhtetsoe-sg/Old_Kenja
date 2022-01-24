<?php
//require_once('dBug.php');

class knjo103Model extends Model {



    var $grade;
    var $cmd;
    var $examyear;
    var $recordTableDiv;
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限


    function knjo103Model()
    {
        $this->examyear = CTRL_YEAR + 1;
    }

    function init()
    {
        $this->valErrMess = array();
        $this->valErrFlg = "";
        $this->new = array();

        $this->cmd   = VARS::request("cmd");
        $this->SCHREGNO = VARS::post("SCHREGNO");
    }

    function updateModel()
    {
        $flg = false;
        
        //プロパティファイル読み込み
        $this->getProperties();

        //DB接続
        $db = Query::dbCheckOut();
        
        //データがあるかどうかチェック
        $query = knjo103Query::countData($this->SCHREGNO);
        $count = $db->getOne($query);
        if($count == 0){
            $this->setMessage("対象データがありません");
            return false;
        }
        
        $data = array();
        
        //データ作成
        $data["利用業務ユニット"] = "AK31";
        
        //送信先
        $query = knjo103Query::getToSchool($this->SCHREGNO, "1");
        $cnt = $db->getOne($query);
        if($cnt > 0){
            $query = knjo103Query::getToSchool($this->SCHREGNO);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            
            if($row["EDU_NNAME"] != ""){    //教育委員会タグを作るかどうか
                if($row["EDU_ID"] != ""){
                    $data["送信先情報"]["教育委員会"]["教育委員会ID"] = $row["EDU_ID"];
                }
                $data["送信先情報"]["教育委員会"]["外字教育委員会名"] = $row["EDU_GNAME"];
                $data["送信先情報"]["教育委員会"]["内字教育委員会名"] = $row["EDU_NNAME"];
                if($row["EDU_SECTION"] != ""){
                    $data["送信先情報"]["教育委員会"]["部課係"] = $row["EDU_SECTION"];
                }
                if($row["EDU_NADDR"] != ""){
                    $data["送信先情報"]["教育委員会"]["所在地"]["lgxml:住所コード"] = $row["EDU_ADDRCD"];
                    $data["送信先情報"]["教育委員会"]["所在地"]["lgxml:外字住所"] = $row["EDU_GADDR"];
                    $data["送信先情報"]["教育委員会"]["所在地"]["lgxml:内字住所"] = $row["EDU_NADDR"];
                    $data["送信先情報"]["教育委員会"]["所在地"]["lgxml:外字方書"] = $row["EDU_GKATAGAKI"];
                    $data["送信先情報"]["教育委員会"]["所在地"]["lgxml:内字方書"] = $row["EDU_NKATAGAKI"];
                    $data["送信先情報"]["教育委員会"]["所在地"]["lgxml:郵便番号"] = $row["EDU_ZIPCD"];
                }
                if($row["EDU_TELNO"] != ""){
                    $data["送信先情報"]["教育委員会"]["電話番号"] = $row["EDU_TELNO"];
                }
                if($row["EDU_SYOKU"] != ""){
                    $data["送信先情報"]["教育委員会"]["職"] = $row["EDU_SYOKU"];
                }
            }
            if($row["SCHOOL_NNAME"] != ""){
                if($row["SCHOOL_ID"] != ""){
                    $data["送信先情報"]["学校"]["学校ID"] = $row["SCHOOL_ID"];
                }
                $data["送信先情報"]["学校"]["外字学校名"] = $row["SCHOOL_GNAME"];
                $data["送信先情報"]["学校"]["内字学校名"] = $row["SCHOOL_NNAME"];
                $data["送信先情報"]["学校"]["所在地"]["lgxml:住所コード"] = $row["SCHOOL_ADDRCD"];
                $data["送信先情報"]["学校"]["所在地"]["lgxml:外字住所"] = $row["SCHOOL_GADDR"];
                $data["送信先情報"]["学校"]["所在地"]["lgxml:内字住所"] = $row["SCHOOL_NADDR"];
                $data["送信先情報"]["学校"]["所在地"]["lgxml:外字方書"] = $row["SCHOOL_GKATAGAKI"];
                $data["送信先情報"]["学校"]["所在地"]["lgxml:内字方書"] = $row["SCHOOL_NKATAGAKI"];
                $data["送信先情報"]["学校"]["所在地"]["lgxml:郵便番号"] = $row["SCHOOL_ZIPCD"];
                if($row["SCHOOL_BUN_ID"] != ""){
                    $data["送信先情報"]["学校"]["分校ID"] = $row["SCHOOL_BUN_ID"];
                }
                if($row["SCHOOL_BUN_GNAME"] != ""){
                    $data["送信先情報"]["学校"]["外字分校名"] = $row["SCHOOL_BUN_GNAME"];
                }
                if($row["SCHOOL_BUN_NNAME"] != ""){
                    $data["送信先情報"]["学校"]["内字分校名"] = $row["SCHOOL_BUN_NNAME"];
                }
                if($row["SCHOOL_BUN_NADDR"] != ""){
                    $data["送信先情報"]["学校"]["分校所在地"]["lgxml:住所コード"] = $row["SCHOOL_BUN_ADDRCD"];
                    $data["送信先情報"]["学校"]["分校所在地"]["lgxml:外字住所"] = $row["SCHOOL_BUN_GADDR"];
                    $data["送信先情報"]["学校"]["分校所在地"]["lgxml:内字住所"] = $row["SCHOOL_BUN_NADDR"];
                    $data["送信先情報"]["学校"]["分校所在地"]["lgxml:外字方書"] = $row["SCHOOL_BUN_GKATAGAKI"];
                    $data["送信先情報"]["学校"]["分校所在地"]["lgxml:内字方書"] = $row["SCHOOL_BUN_NKATAGAKI"];
                    $data["送信先情報"]["学校"]["分校所在地"]["lgxml:郵便番号"] = $row["SCHOOL_BUN_ZIPCD"];
                }
                if($row["SCHOOL_TELNO"] != ""){
                    $data["送信先情報"]["学校"]["電話番号"] = $row["SCHOOL_TELNO"];
                }
                if($row["SCHOOL_SYOKU"] != ""){
                    $data["送信先情報"]["学校"]["職"] = $row["SCHOOL_SYOKU"];
                }
                if($row["SCHOOL_BIKOU"] != ""){
                    $data["送信先情報"]["学校"]["備考"] = $row["SCHOOL_BIKOU"];
                }
            }
        }else{
            $data["送信先情報"] = "";
        }
        
        //送信元
        $query = knjo103Query::getFromSchool($this->SCHREGNO, "1");
        $cnt = $db->getOne($query);
        if($cnt > 0){
            $query = knjo103Query::getFromSchool($this->SCHREGNO);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            
            if($row["EDU_NNAME"] != ""){    //教育委員会タグを作るかどうか
                if($row["EDU_ID"] != ""){
                    $data["送信元情報"]["教育委員会"]["教育委員会ID"] = $row["EDU_ID"];
                }
                $data["送信元情報"]["教育委員会"]["外字教育委員会名"] = $row["EDU_GNAME"];
                $data["送信元情報"]["教育委員会"]["内字教育委員会名"] = $row["EDU_NNAME"];
                if($row["EDU_SECTION"] != ""){
                    $data["送信元情報"]["教育委員会"]["部課係"] = $row["EDU_SECTION"];
                }
                if($row["EDU_NADDR"] != ""){
                    $data["送信元情報"]["教育委員会"]["所在地"]["lgxml:住所コード"] = $row["EDU_ADDRCD"];
                    $data["送信元情報"]["教育委員会"]["所在地"]["lgxml:外字住所"] = $row["EDU_GADDR"];
                    $data["送信元情報"]["教育委員会"]["所在地"]["lgxml:内字住所"] = $row["EDU_NADDR"];
                    $data["送信元情報"]["教育委員会"]["所在地"]["lgxml:外字方書"] = $row["EDU_GKATAGAKI"];
                    $data["送信元情報"]["教育委員会"]["所在地"]["lgxml:内字方書"] = $row["EDU_NKATAGAKI"];
                    $data["送信元情報"]["教育委員会"]["所在地"]["lgxml:郵便番号"] = $row["EDU_ZIPCD"];
                }
                if($row["EDU_TELNO"] != ""){
                    $data["送信元情報"]["教育委員会"]["電話番号"] = $row["EDU_TELNO"];
                }
                if($row["EDU_SYOKU"] != ""){
                    $data["送信元情報"]["教育委員会"]["職"] = $row["EDU_SYOKU"];
                }
            }
            if($row["SCHOOL_NNAME"] != ""){
                if($row["SCHOOL_ID"] != ""){
                    $data["送信元情報"]["学校"]["学校ID"] = $row["SCHOOL_ID"];
                }
                $data["送信元情報"]["学校"]["外字学校名"] = $row["SCHOOL_GNAME"];
                $data["送信元情報"]["学校"]["内字学校名"] = $row["SCHOOL_NNAME"];
                $data["送信元情報"]["学校"]["所在地"]["lgxml:住所コード"] = $row["SCHOOL_ADDRCD"];
                $data["送信元情報"]["学校"]["所在地"]["lgxml:外字住所"] = $row["SCHOOL_GADDR"];
                $data["送信元情報"]["学校"]["所在地"]["lgxml:内字住所"] = $row["SCHOOL_NADDR"];
                $data["送信元情報"]["学校"]["所在地"]["lgxml:外字方書"] = $row["SCHOOL_GKATAGAKI"];
                $data["送信元情報"]["学校"]["所在地"]["lgxml:内字方書"] = $row["SCHOOL_NKATAGAKI"];
                $data["送信元情報"]["学校"]["所在地"]["lgxml:郵便番号"] = $row["SCHOOL_ZIPCD"];
                if($row["SCHOOL_BUN_ID"] != ""){
                    $data["送信元情報"]["学校"]["分校ID"] = $row["SCHOOL_BUN_ID"];
                }
                if($row["SCHOOL_BUN_GNAME"] != ""){
                    $data["送信元情報"]["学校"]["外字分校名"] = $row["SCHOOL_BUN_GNAME"];
                }
                if($row["SCHOOL_BUN_NNAME"] != ""){
                    $data["送信元情報"]["学校"]["内字分校名"] = $row["SCHOOL_BUN_NNAME"];
                }
                if($row["SCHOOL_BUN_NADDR"] != ""){
                    $data["送信元情報"]["学校"]["分校所在地"]["lgxml:住所コード"] = $row["SCHOOL_BUN_ADDRCD"];
                    $data["送信元情報"]["学校"]["分校所在地"]["lgxml:外字住所"] = $row["SCHOOL_BUN_GADDR"];
                    $data["送信元情報"]["学校"]["分校所在地"]["lgxml:内字住所"] = $row["SCHOOL_BUN_NADDR"];
                    $data["送信元情報"]["学校"]["分校所在地"]["lgxml:外字方書"] = $row["SCHOOL_BUN_GKATAGAKI"];
                    $data["送信元情報"]["学校"]["分校所在地"]["lgxml:内字方書"] = $row["SCHOOL_BUN_NKATAGAKI"];
                    $data["送信元情報"]["学校"]["分校所在地"]["lgxml:郵便番号"] = $row["SCHOOL_BUN_ZIPCD"];
                }
                if($row["SCHOOL_TELNO"] != ""){
                    $data["送信元情報"]["学校"]["電話番号"] = $row["SCHOOL_TELNO"];
                }
                if($row["SCHOOL_SYOKU"] != ""){
                    $data["送信元情報"]["学校"]["職"] = $row["SCHOOL_SYOKU"];
                }
                if($row["SCHOOL_BIKOU"] != ""){
                    $data["送信元情報"]["学校"]["備考"] = $row["SCHOOL_BIKOU"];
                }
            }
        }else{
            $data["送信元情報"] = "";
        }
        
        //指導要録情報
        $query = knjo103Query::getSyubetu($this->SCHREGNO);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        
        //指導要録種別
        $data["指導要録情報"]["指導要録種別"] = $row["YOUROKU_SYUBETU"];
        if($row["YOUROKU_SYUBETU"] == "K01"){
            $this->SYUBETU = 0;
        }else if($row["YOUROKU_SYUBETU"] == "K02"){  //身体障害者・病弱者高等部判断用フラグ
            $this->SYUBETU = 1;
        }else{  //知的障害者高等部
            $this->SYUBETU = 2;
        }
        
        //指導要録情報＞学校変更状況
        $query = knjo103Query::getSchChange($this->SCHREGNO);
        $result = $db->query($query);
        
        $cnt = 0;
        while($schChange = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $data["指導要録情報"]["学校変更状況"][$cnt]["年度"] = $schChange["YEAR"];
            $data["指導要録情報"]["学校変更状況"][$cnt]["教育課程の区分"] = $schChange["KUBUN"];
            $data["指導要録情報"]["学校変更状況"][$cnt]["学年"] = $schChange["GRADE"];
            if($schChange["STAY_CNT"] != "" && $schChange["STAY_CNT"] != 0){
                $data["指導要録情報"]["学校変更状況"][$cnt]["原級留置回数"] = $schChange["STAY_CNT"];
            }
            //校長氏名
            $query = knjo103Query::getPrincipal($this->SCHREGNO, $schChange["DATA_ROW"]);
            $presult = $db->query($query);
            $priCnt = 0;
            while($priRow = $presult->fetchRow(DB_FETCHMODE_ASSOC)){
                $data["指導要録情報"]["学校変更状況"][$cnt]["校長氏名"][$priCnt]["氏名"]["lgxml:外字氏名"] = $priRow["GNAME"];
                $data["指導要録情報"]["学校変更状況"][$cnt]["校長氏名"][$priCnt]["氏名"]["lgxml:内字氏名"] = $priRow["NNAME"];
                $data["指導要録情報"]["学校変更状況"][$cnt]["校長氏名"][$priCnt]["氏名"]["lgxml:フリガナ"] = $priRow["NAME_KANA"];
                
                $priCnt++;
            }
            //ホームルーム担任者氏名
            $query = knjo103Query::getHrteacher($this->SCHREGNO, $schChange["DATA_ROW"]);
            $hresult = $db->query($query);
            $hrCnt = 0;
            while($hrRow = $hresult->fetchRow(DB_FETCHMODE_ASSOC)){
                $data["指導要録情報"]["学校変更状況"][$cnt]["ホームルーム担任者氏名"][$hrCnt]["氏名"]["lgxml:外字氏名"] = $hrRow["GNAME"];
                $data["指導要録情報"]["学校変更状況"][$cnt]["ホームルーム担任者氏名"][$hrCnt]["氏名"]["lgxml:内字氏名"] = $hrRow["NNAME"];
                $data["指導要録情報"]["学校変更状況"][$cnt]["ホームルーム担任者氏名"][$hrCnt]["氏名"]["lgxml:フリガナ"] = $hrRow["NAME_KANA"];
                
                $hrCnt++;
            }
            
            $cnt++;
        }
        
        //指導要録情報＞学籍の記録
        if($row["STUDENT_ID"] != ""){
            $data["指導要録情報"]["学籍の記録"]["生徒"]["生徒ID"] = $row["STUDENT_ID"];
        }
        $data["指導要録情報"]["学籍の記録"]["生徒"]["氏名"]["lgxml:外字氏名"] = $row["STUDENT_GNAME"];
        $data["指導要録情報"]["学籍の記録"]["生徒"]["氏名"]["lgxml:内字氏名"] = $row["STUDENT_NNAME"];
        $data["指導要録情報"]["学籍の記録"]["生徒"]["氏名"]["lgxml:フリガナ"] = $row["STUDENT_KANA"];
        
        //指導要録情報＞学籍の記録＞生徒＞通称名
        $query = knjo103Query::getAlias($this->SCHREGNO, "1");
        $aliasCnt = $db->getOne($query);
        if($aliasCnt > 0){
            $query = knjo103Query::getAlias($this->SCHREGNO);
            $result = $db->query($query);
            $cnt = 0;
            while($aliasRow = $result->fetchRow(DB_FETCHNODE_ASSOC)){
                $data["指導要録情報"]["学籍の記録"]["生徒"]["通称名"][$cnt]["lgxml:外字氏名"] = $aliasRow["GNAME"];
                $data["指導要録情報"]["学籍の記録"]["生徒"]["通称名"][$cnt]["lgxml:内字氏名"] = $aliasRow["NNAME"];
                $data["指導要録情報"]["学籍の記録"]["生徒"]["通称名"][$cnt]["lgxml:フリガナ"] = $aliasRow["NAME_KANA"];
                
                $cnt++;
            }
        }
        
        //指導要録情報＞学籍の記録＞生徒＞
        $data["指導要録情報"]["学籍の記録"]["生徒"]["性別"] = $row["STUDENT_SEX"];
        $data["指導要録情報"]["学籍の記録"]["生徒"]["生年月日"] = $row["STUDENT_BIRTHDAY"];
        
        if($row["STUDENT_NADDR"] != ""){
            $data["指導要録情報"]["学籍の記録"]["生徒"]["現住所"]["住所"]["lgxml:住所コード"] = $row["STUDENT_ADDRCD"];
            $data["指導要録情報"]["学籍の記録"]["生徒"]["現住所"]["住所"]["lgxml:外字住所"] = $row["STUDENT_GADDR"];
            $data["指導要録情報"]["学籍の記録"]["生徒"]["現住所"]["住所"]["lgxml:内字住所"] = $row["STUDENT_NADDR"];
            $data["指導要録情報"]["学籍の記録"]["生徒"]["現住所"]["住所"]["lgxml:外字方書"] = $row["STUDENT_GKATAGAKI"];
            $data["指導要録情報"]["学籍の記録"]["生徒"]["現住所"]["住所"]["lgxml:内字方書"] = $row["STUDENT_NKATAGAKI"];
            $data["指導要録情報"]["学籍の記録"]["生徒"]["現住所"]["住所"]["lgxml:郵便番号"] = $row["STUDENT_ZIPCD"];
            if($row["STUDENT_BIKOU"] != ""){
                $data["指導要録情報"]["学籍の記録"]["生徒"]["現住所"]["備考"] = $row["STUDENT_BIKOU"];
            }
        }
        
        //指導要録情報＞学籍の記録＞生徒＞現住所_その他
        $query = knjo103Query::getStOtherAdd($this->SCHREGNO, "1");
        $addCnt = $db->getOne($query);
        if($addCnt > 0){
            $query = knjo103Query::getStOtherAdd($this->SCHREGNO);
            $result = $db->query($query);
            $cnt = 0;
            
            while($otrAdd = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $data["指導要録情報"]["学籍の記録"]["生徒"]["現住所_その他"][$cnt]["住所"]["lgxml:住所コード"] = $otrAdd["ADDRCD"];
                $data["指導要録情報"]["学籍の記録"]["生徒"]["現住所_その他"][$cnt]["住所"]["lgxml:外字住所"] = $otrAdd["GADDR"];
                $data["指導要録情報"]["学籍の記録"]["生徒"]["現住所_その他"][$cnt]["住所"]["lgxml:内字住所"] = $otrAdd["NADDR"];
                $data["指導要録情報"]["学籍の記録"]["生徒"]["現住所_その他"][$cnt]["住所"]["lgxml:外字方書"] = $otrAdd["GKATAGAKI"];
                $data["指導要録情報"]["学籍の記録"]["生徒"]["現住所_その他"][$cnt]["住所"]["lgxml:内字方書"] = $otrAdd["NKATAGAKI"];
                $data["指導要録情報"]["学籍の記録"]["生徒"]["現住所_その他"][$cnt]["住所"]["lgxml:郵便番号"] = $otrAdd["ZIPCD"];
                if($otrAdd["BIKOU"] != ""){
                    $data["指導要録情報"]["学籍の記録"]["生徒"]["現住所_その他"][$cnt]["備考"] = $otrAdd["BIKOU"];
                }
                
                $cnt++;
            }
        }
        
        //指導要録情報＞学籍の記録＞生徒＞国籍
        if($row["STUDENT_NATION"] != ""){
            $data["指導要録情報"]["学籍の記録"]["生徒"]["国籍"] = $row["STUDENT_NATION"];
        }
        
        //指導要録情報＞学籍の記録＞生徒＞保護者
        $query = knjo103Query::getParent($this->SCHREGNO);
        $presult = $db->query($query);
        
        $count = 0;
        
        while($parentRow = $presult->fetchRow(DB_FETCHMODE_ASSOC)){
            if($parentRow["PARENT_ID"] != ""){
                $data["指導要録情報"]["学籍の記録"]["生徒"]["保護者"][$count]["保護者ID"] = $parentRow["PARENT_ID"];
            }
            $data["指導要録情報"]["学籍の記録"]["生徒"]["保護者"][$count]["氏名"]["lgxml:外字氏名"] = $parentRow["GNAME"];
            $data["指導要録情報"]["学籍の記録"]["生徒"]["保護者"][$count]["氏名"]["lgxml:内字氏名"] = $parentRow["NNAME"];
            $data["指導要録情報"]["学籍の記録"]["生徒"]["保護者"][$count]["氏名"]["lgxml:フリガナ"] = $parentRow["NAME_KANA"];
            $data["指導要録情報"]["学籍の記録"]["生徒"]["保護者"][$count]["現住所"]["住所"]["lgxml:住所コード"] = $parentRow["ADDRCD"];
            $data["指導要録情報"]["学籍の記録"]["生徒"]["保護者"][$count]["現住所"]["住所"]["lgxml:外字住所"] = $parentRow["GADDR"];
            $data["指導要録情報"]["学籍の記録"]["生徒"]["保護者"][$count]["現住所"]["住所"]["lgxml:内字住所"] = $parentRow["NADDR"];
            $data["指導要録情報"]["学籍の記録"]["生徒"]["保護者"][$count]["現住所"]["住所"]["lgxml:外字方書"] = $parentRow["GKATAGAKI"];
            $data["指導要録情報"]["学籍の記録"]["生徒"]["保護者"][$count]["現住所"]["住所"]["lgxml:内字方書"] = $parentRow["NKATAGAKI"];
            $data["指導要録情報"]["学籍の記録"]["生徒"]["保護者"][$count]["現住所"]["住所"]["lgxml:郵便番号"] = $parentRow["ZIPCD"];
            if($parentRow["BIKOU"] != ""){
                $data["指導要録情報"]["学籍の記録"]["生徒"]["保護者"][$count]["現住所"]["備考"] = $parentRow["BIKOU"];
            }
            
            //保護者＞現住所_その他
            $query = knjo103Query::getParentOtrAdd($this->SCHREGNO, $parentRow["DATA_ROW"], "1");
            $pOtrCnt = $db->getOne($query);
            if($pOtrCnt > 0){
                $query = knjo103Query::getParentOtrAdd($this->SCHREGNO, $parentRow["DATA_ROW"]);
                $result = $db->query($query);
                $poCnt = 0;
                while($poRow = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                    $data["指導要録情報"]["学籍の記録"]["生徒"]["保護者"][$count]["現住所_その他"][$poCnt]["住所"]["lgxml:住所コード"] = $poRow["ADDRCD"];
                    $data["指導要録情報"]["学籍の記録"]["生徒"]["保護者"][$count]["現住所_その他"][$poCnt]["住所"]["lgxml:外字住所"] = $poRow["GADDR"];
                    $data["指導要録情報"]["学籍の記録"]["生徒"]["保護者"][$count]["現住所_その他"][$poCnt]["住所"]["lgxml:内字住所"] = $poRow["NADDR"];
                    $data["指導要録情報"]["学籍の記録"]["生徒"]["保護者"][$count]["現住所_その他"][$poCnt]["住所"]["lgxml:外字方書"] = $poRow["GKATAGAKI"];
                    $data["指導要録情報"]["学籍の記録"]["生徒"]["保護者"][$count]["現住所_その他"][$poCnt]["住所"]["lgxml:内字方書"] = $poRow["NKATAGAKI"];
                    $data["指導要録情報"]["学籍の記録"]["生徒"]["保護者"][$count]["現住所_その他"][$poCnt]["住所"]["lgxml:郵便番号"] = $poRow["ZIPCD"];
                    if($poRow["BIKOU"] != ""){
                        $data["指導要録情報"]["学籍の記録"]["生徒"]["保護者"][$count]["現住所_その他"][$poCnt]["備考"] = $poRow["BIKOU"];
                    }
                    
                    $poCnt++;
                }
                
            }
            
            $count++;
        }
        
        //指導要録情報＞学籍の記録＞在学状況
        if($row["SCHOOL_ID"] != ""){
            $data["指導要録情報"]["学籍の記録"]["在学状況"]["学校情報"]["学校ID"] = $row["SCHOOL_ID"];
        }
        $data["指導要録情報"]["学籍の記録"]["在学状況"]["学校情報"]["外字学校名"] = $row["SCHOOL_GNAME"];
        $data["指導要録情報"]["学籍の記録"]["在学状況"]["学校情報"]["内字学校名"] = $row["SCHOOL_NNAME"];
        $data["指導要録情報"]["学籍の記録"]["在学状況"]["学校情報"]["所在地"]["lgxml:住所コード"] = $row["SCHOOL_ADDRCD"];
        $data["指導要録情報"]["学籍の記録"]["在学状況"]["学校情報"]["所在地"]["lgxml:外字住所"] = $row["SCHOOL_GADDR"];
        $data["指導要録情報"]["学籍の記録"]["在学状況"]["学校情報"]["所在地"]["lgxml:内字住所"] = $row["SCHOOL_NADDR"];
        $data["指導要録情報"]["学籍の記録"]["在学状況"]["学校情報"]["所在地"]["lgxml:外字方書"] = $row["SCHOOL_GKATAGAKI"];
        $data["指導要録情報"]["学籍の記録"]["在学状況"]["学校情報"]["所在地"]["lgxml:内字方書"] = $row["SCHOOL_NKATAGAKI"];
        $data["指導要録情報"]["学籍の記録"]["在学状況"]["学校情報"]["所在地"]["lgxml:郵便番号"] = $row["SCHOOL_ZIPCD"];
        if($row["SCHOOL_BUN_ID"] != ""){
            $data["指導要録情報"]["学籍の記録"]["在学状況"]["学校情報"]["分校ID"] = $row["SCHOOL_BUN_ID"];
        }
        if($row["SCHOOL_BUN_GNAME"] != ""){
            $data["指導要録情報"]["学籍の記録"]["在学状況"]["学校情報"]["外字分校名"] = $row["SCHOOL_BUN_GNAME"];
        }
        if($row["SCHOOL_BUN_NNAME"] != ""){
            $data["指導要録情報"]["学籍の記録"]["在学状況"]["学校情報"]["内字分校名"] = $row["SCHOOL_BUN_NNAME"];
        }
        if($row["SCHOOL_BUN_NADDR"] != ""){
            $data["指導要録情報"]["学籍の記録"]["在学状況"]["学校情報"]["分校所在地"]["lgxml:住所コード"] = $row["SCHOOL_BUN_ADDRCD"];
            $data["指導要録情報"]["学籍の記録"]["在学状況"]["学校情報"]["分校所在地"]["lgxml:外字住所"] = $row["SCHOOL_BUN_GADDR"];
            $data["指導要録情報"]["学籍の記録"]["在学状況"]["学校情報"]["分校所在地"]["lgxml:内字住所"] = $row["SCHOOL_BUN_NADDR"];
            $data["指導要録情報"]["学籍の記録"]["在学状況"]["学校情報"]["分校所在地"]["lgxml:外字方書"] = $row["SCHOOL_BUN_GKATAGAKI"];
            $data["指導要録情報"]["学籍の記録"]["在学状況"]["学校情報"]["分校所在地"]["lgxml:内字方書"] = $row["SCHOOL_BUN_NKATAGAKI"];
            $data["指導要録情報"]["学籍の記録"]["在学状況"]["学校情報"]["分校所在地"]["lgxml:郵便番号"] = $row["SCHOOL_BUN_ZIPCD"];
        }
        if($row["SCHOOL_TELNO"] != ""){
            $data["指導要録情報"]["学籍の記録"]["在学状況"]["学校情報"]["電話番号"] = $row["SCHOOL_TELNO"];
        }
        if($row["SCHOOL_SYOKU"] != ""){
            $data["指導要録情報"]["学籍の記録"]["在学状況"]["学校情報"]["職"] = $row["SCHOOL_SYOKU"];
        }
        if($row["SCHOOL_BIKOU"] != ""){
            $data["指導要録情報"]["学籍の記録"]["在学状況"]["学校情報"]["備考"] = $row["SCHOOL_BIKOU"];
        }
        
        
        //学籍の記録＞在学状況＞在学学年
        $query = knjo103Query::getZaigaku($this->SCHREGNO);
        $result = $db->query($query);
        
        $count = 0;
        
        while($zaiRow = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $data["指導要録情報"]["学籍の記録"]["在学状況"]["在学学年"][$count]["年度"] = $zaiRow["YEAR"];
            $data["指導要録情報"]["学籍の記録"]["在学状況"]["在学学年"][$count]["学年または年次"] = $zaiRow["GRADE"];
            $data["指導要録情報"]["学籍の記録"]["在学状況"]["在学学年"][$count]["ホームルーム"] = $zaiRow["HR_CLASS"];
            $data["指導要録情報"]["学籍の記録"]["在学状況"]["在学学年"][$count]["整理番号"] = $zaiRow["ATTENDNO"];
            
            $count++;
        }
        
        //異動状況＞入学前の経歴
        $query = knjo103Query::getKeireki($this->SCHREGNO, "1");
        $krkCnt = $db->getOne($query);
        if($krkCnt > 0){
            $query = knjo103Query::getKeireki($this->SCHREGNO);
            $result = $db->query($query);
            
            $cnt = 0;
            
            while($krkRow = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $data["指導要録情報"]["学籍の記録"]["異動状況"]["入学前の経歴"][$cnt] = $krkRow["KEIREKI"];
                
                $cnt++;
            }
        }
        
        //指導要録情報＞学籍の記録＞異動状況
        $query = knjo103Query::getIdouMst($this->SCHREGNO);
        $row = $db->getRow($query, DB_FETCHMODE_ASOOC);
        
        //入学or編入学or転入学
        if($row["IN_FLG"] == "1"){
            $top = "入学";
        }else if($row["IN_FLG"] == "2"){
            $top = "編入学";
        }else{
            $top = "転入学";
        }
        
        $data["指導要録情報"]["学籍の記録"]["異動状況"][$top][$top."年月日"] = $row["IN_DATE"];
        if($row["IN_GRADE"] != ""){
            $data["指導要録情報"]["学籍の記録"]["異動状況"][$top][$top."時学年"] = $row["IN_GRADE"];
        }
        if($row["ZAIGAKU_FROM"] != ""){
            $data["指導要録情報"]["学籍の記録"]["異動状況"][$top]["在学すべき期間_開始年月日"] = $row["ZAIGAKU_FROM"];
        }
        if($row["ZAIGAKU_TO"] != ""){
            $data["指導要録情報"]["学籍の記録"]["異動状況"][$top]["在学すべき期間_終了年月日"] = $row["ZAIGAKU_TO"];
        }
        if($row["ENT_BIKOU"] != ""){
            $data["指導要録情報"]["学籍の記録"]["異動状況"][$top]["備考"] = $row["ENT_BIKOU"];
        }
        
        //転学or退学or卒業
        if($row["OUT_FLG"] != ""){
            if($row["OUT_FLG"] == "1"){
                $top = "転学";
            }else if($row["OUT_FLG"] == "2"){
                $top = "退学";
            }else{
                $top = "卒業";
            }
            
            $data["指導要録情報"]["学籍の記録"]["異動状況"][$top][$top."年月日"] = $row["OUT_DATE"];
            if($row["LEAVE_DATE"] != ""){
                $data["指導要録情報"]["学籍の記録"]["異動状況"][$top]["学校を去った日"] = $row["LEAVE_DATE"];
            }
            if($row["OUT_DATE"] != ""){
                $data["指導要録情報"]["学籍の記録"]["異動状況"][$top]["備考"] = $row["OUT_BIKOU"];
            }
        }
        
        //留学等
        $query = knjo103Query::getAbroad($this->SCHREGNO, "1");
        $abrCnt = $db->getOne($query);
        
        if($abrCnt > 0){
            $query = knjo103Query::getAbroad($this->SCHREGNO);
            $result = $db->query($query);
            
            $cnt = 0;
            
            while($abrRow = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $data["指導要録情報"]["学籍の記録"]["異動状況"]["留学等"][$cnt]["留学開始年月日"] = $abrRow["START_DATE"];
                $data["指導要録情報"]["学籍の記録"]["異動状況"]["留学等"][$cnt]["留学終了年月日"] = $abrRow["END_DATE"];
                if($abrRow["BIKOU"] != ""){
                    $data["指導要録情報"]["学籍の記録"]["異動状況"]["留学等"][$cnt]["備考"] = $abrRow["BIKOU"];
                }
                
                $cnt++;
            }
        }
        
        //進学先就職先等
        if($row["AFTER_LEAVE"] != ""){
            $data["指導要録情報"]["学籍の記録"]["異動状況"]["進学先就職先等"] = $row["AFTER_LEAVE"];
        }
        
        
        //学籍の記録＞課程名・学科名
        $data["指導要録情報"]["学籍の記録"]["課程名"] = $row["KATEI_NAME"];
        $data["指導要録情報"]["学籍の記録"]["学科名"] = $row["GAKKA_NAME"];
        
        
        //指導要録情報＞各教科_科目等の修得単位数の記録
        if($this->SYUBETU != 2){    //知的障害者のときは使用しない
            //各学科に共通する各教科_科目
            $query = knjo103Query::getCmnSubj($this->SCHREGNO, "1");
            $csCnt = $db->getOne($query);
            
            if($csCnt > 0){
                $query = knjo103Query::getCmnSubj($this->SCHREGNO);
                $result = $db->query($query);
                
                $cnt = 0;
                
                while($cmnRow = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                    if($cmnRow["CLASS_NAME"] != ""){
                        $data["指導要録情報"]["各教科_科目等の修得単位数の記録"]["各学科に共通する各教科_科目"][$cnt]["教科"] = $cmnRow["CLASS_NAME"];
                    }
                    if($cmnRow["SUBCLASS_NAME"] != ""){
                        $data["指導要録情報"]["各教科_科目等の修得単位数の記録"]["各学科に共通する各教科_科目"][$cnt]["科目"]["科目名"] = $cmnRow["SUBCLASS_NAME"];
                        $data["指導要録情報"]["各教科_科目等の修得単位数の記録"]["各学科に共通する各教科_科目"][$cnt]["科目"]["修得単位数"] = $cmnRow["SUBCLASS_TANNI"];
                    }
                    if($cmnRow["SCHOOL_CLASS_NAME"] != ""){
                        $data["指導要録情報"]["各教科_科目等の修得単位数の記録"]["各学科に共通する各教科_科目"][$cnt]["学校設定教科"] = $cmnRow["SCHOOL_CLASS_NAME"];
                    }
                    if($cmnRow["SCHOOL_SUBCLASS_NAME"] != ""){
                        $data["指導要録情報"]["各教科_科目等の修得単位数の記録"]["各学科に共通する各教科_科目"][$cnt]["学校設定科目"]["学校設定科目名"] = $cmnRow["SCHOOL_SUBCLASS_NAME"];
                        $data["指導要録情報"]["各教科_科目等の修得単位数の記録"]["各学科に共通する各教科_科目"][$cnt]["学校設定科目"]["修得単位数"] = $cmnRow["SCHOOL_SUBCLASS_TANNI"];
                    }
                    
                    $cnt++;
                }
            }
            
            //主として専門学科において開設される各教科_科目
            $query = knjo103Query::getExpSubj($this->SCHREGNO, "1");
            $esCnt = $db->getOne($query);
            
            if($esCnt > 0){
                
                $query = knjo103Query::getExpSubj($this->SCHREGNO);
                $result = $db->query($query);
                
                $cnt = 0;
                
                while($expRow = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                    if($expRow["CLASS_NAME"] != ""){
                        $data["指導要録情報"]["各教科_科目等の修得単位数の記録"]["主として専門学科において開設される各教科_科目"][$cnt]["教科"] = $expRow["CLASS_NAME"];
                    }
                    if($expRow["SUBCLASS_NAME"] != ""){
                        $data["指導要録情報"]["各教科_科目等の修得単位数の記録"]["主として専門学科において開設される各教科_科目"][$cnt]["科目"]["科目名"] = $expRow["SUBCLASS_NAME"];
                        $data["指導要録情報"]["各教科_科目等の修得単位数の記録"]["主として専門学科において開設される各教科_科目"][$cnt]["科目"]["修得単位数"] = $expRow["SUBCLASS_TANNI"];
                    }
                    if($expRow["SCHOOL_CLASS_NAME"] != ""){
                        $data["指導要録情報"]["各教科_科目等の修得単位数の記録"]["主として専門学科において開設される各教科_科目"][$cnt]["学校設定教科"] = $expRow["SCHOOL_CLASS_NAME"];
                    }
                    if($expRow["SCHOOL_SUBCLASS_NAME"] != ""){
                        $data["指導要録情報"]["各教科_科目等の修得単位数の記録"]["主として専門学科において開設される各教科_科目"][$cnt]["学校設定科目"]["学校設定科目名"] = $expRow["SCHOOL_SUBCLASS_NAME"];
                        $data["指導要録情報"]["各教科_科目等の修得単位数の記録"]["主として専門学科において開設される各教科_科目"][$cnt]["学校設定科目"]["修得単位数"] = $expRow["SCHOOL_SUBCLASS_TANNI"];
                    }
                    
                    $cnt++;
                }
            }
            
            if($row["ABROAD_TANNI"] != ""){
                $data["指導要録情報"]["各教科_科目等の修得単位数の記録"]["留学"]["修得単位数"] = $row["ABROAD_TANNI"];
            }
            if($row["ZIRITSU_TANNI"] != ""){
                $data["指導要録情報"]["各教科_科目等の修得単位数の記録"]["自立活動"]["修得単位数"] = $row["ZIRITSU_TANNI"];
            }
            if($row["INTEGRATED_TANNI"] != "" || $row["INTEGRATED_NAME"] != ""){
                $data["指導要録情報"]["各教科_科目等の修得単位数の記録"]["総合的な学習の時間"]["総合的な学習の名称"] = $row["INTEGRATED_NAME"];
                $data["指導要録情報"]["各教科_科目等の修得単位数の記録"]["総合的な学習の時間"]["修得単位数"] = $row["INTEGRATED_TANNI"];
            }
        }
        if($this->SYUBETU != 2){
            //指導要録情報＞指導に関する記録1($this->SYUBETU != 2のとき(知的障害者以外のとき)のみ)
            $query = knjo103Query::getShidou1($this->SCHREGNO, "1");
            $shiFcnt = $db->getOne($query);
            
            if($shiFcnt > 0){
                $query = knjo103Query::getShidou1($this->SCHREGNO);
                $shiFresult = $db->query($query);
                
                $cnt = 0;
                
                while($shiFrow = $shiFresult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $data["指導要録情報"]["指導に関する記録1"][$cnt]["様式種別"] = $shiFrow["SYUBETU"];
                    $data["指導要録情報"]["指導に関する記録1"][$cnt]["学年"] = $shiFrow["GRADE"];
                    if($shiFrow["STAY_CNT"] != "0" && $shiFrow["STAY_CNT"] != ""){
                        $data["指導要録情報"]["指導に関する記録1"][$cnt]["原級留置回数"] = $shiFrow["STAY_CNT"];
                    }
                    
                    //各学科に共通する各教科_科目
                    $query = knjo103Query::getShi1CmnSubj($this->SCHREGNO, $shiFrow["DATA_ROW"], "1");
                    $cmnCnt = $db->getOne($query);
                    if($cmnCnt > 0){
                        $query = knjo103Query::getShi1CmnSubj($this->SCHREGNO, $shiFrow["DATA_ROW"]);
                        $result = $db->query($query);
                        $count = 0;
                        
                        while($cmnRow = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                            if($cmnRow["CLASS_NAME"] != ""){
                                $data["指導要録情報"]["指導に関する記録1"][$cnt]["各学科に共通する各教科_科目"][$count]["教科"] = $cmnRow["CLASS_NAME"];
                            }
                            if($cmnRow["SUBCLASS_NAME"] != ""){
                                $data["指導要録情報"]["指導に関する記録1"][$cnt]["各学科に共通する各教科_科目"][$count]["科目"]["科目名"] = $cmnRow["SUBCLASS_NAME"];
                                $data["指導要録情報"]["指導に関する記録1"][$cnt]["各学科に共通する各教科_科目"][$count]["科目"]["評定"] = $cmnRow["SUBCLASS_HYOUTEI"];
                                $data["指導要録情報"]["指導に関する記録1"][$cnt]["各学科に共通する各教科_科目"][$count]["科目"]["修得単位数"] = $cmnRow["SUBCLASS_TANNI"];
                                $data["指導要録情報"]["指導に関する記録1"][$cnt]["各学科に共通する各教科_科目"][$count]["科目"]["修得単位数の計"] = $cmnRow["SUBCLASS_TANNI_TOTAL"];
                                if($cmnRow["SUBCLASS_BIKOU"] != ""){
                                    $data["指導要録情報"]["指導に関する記録1"][$cnt]["各学科に共通する各教科_科目"][$count]["科目"]["備考"] = $cmnRow["SUBCLASS_BIKOU"];
                                }
                            }
                            if($cmnRow["SCHOOL_CLASS_NAME"] != ""){
                                $data["指導要録情報"]["指導に関する記録1"][$cnt]["各学科に共通する各教科_科目"][$count]["学校設定教科"] = $cmnRow["SCHOOL_CLASS_NAME"];
                            }
                            if($cmnRow["SCHOOL_SUBCLASS_NAME"] != ""){
                                $data["指導要録情報"]["指導に関する記録1"][$cnt]["各学科に共通する各教科_科目"][$count]["学校設定科目"]["学校設定科目名"] = $cmnRow["SCHOOL_SUBCLASS_NAME"];
                                $data["指導要録情報"]["指導に関する記録1"][$cnt]["各学科に共通する各教科_科目"][$count]["学校設定科目"]["学校設定科目評定"] = $cmnRow["SCHOOL_SUBCLASS_HYOUTEI"];
                                $data["指導要録情報"]["指導に関する記録1"][$cnt]["各学科に共通する各教科_科目"][$count]["学校設定科目"]["学校設定科目修得単位数"] = $cmnRow["SCHOOL_SUBCLASS_TANNI"];
                                $data["指導要録情報"]["指導に関する記録1"][$cnt]["各学科に共通する各教科_科目"][$count]["学校設定科目"]["学校設定科目修得単位数の計"] = $cmnRow["SCHOOL_SUBCLASS_TANNI_TOTAL"];
                                if($cmnRow["SCHOOL_SUBCLASS_BIKOU"] != ""){
                                    $data["指導要録情報"]["指導に関する記録1"][$cnt]["各学科に共通する各教科_科目"][$count]["学校設定科目"]["学校設定科目備考"] = $cmnRow["SCHOOL_SUBCLASS_BIKOU"];
                                }
                            }
                            
                            $count++;
                        }
                    }
                    
                    //主として専門学科において開設される各教科_科目
                    $query = knjo103Query::getShi1ExpSubj($this->SCHREGNO, $shiFrow["DATA_ROW"], "1");
                    $expCnt = $db->getOne($query);
                    if($expCnt > 0){
                        $query = knjo103Query::getShi1ExpSubj($this->SCHREGNO, $shiFrow["DATA_ROW"]);
                        $result = $db->query($query);
                        
                        $count = 0;
                        
                        while($expRow = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                            if($expRow["CLASS_NAME"] != ""){
                                $data["指導要録情報"]["指導に関する記録1"][$cnt]["主として専門学科において開設される各教科_科目"][$count]["教科"] = $expRow["CLASS_NAME"];
                            }
                            if($expRow["SUBCLASS_NAME"] != ""){
                                $data["指導要録情報"]["指導に関する記録1"][$cnt]["主として専門学科において開設される各教科_科目"][$count]["科目"]["科目名"] = $expRow["SUBCLASS_NAME"];
                                $data["指導要録情報"]["指導に関する記録1"][$cnt]["主として専門学科において開設される各教科_科目"][$count]["科目"]["評定"] = $expRow["SUBCLASS_HYOUTEI"];
                                $data["指導要録情報"]["指導に関する記録1"][$cnt]["主として専門学科において開設される各教科_科目"][$count]["科目"]["修得単位数"] = $expRow["SUBCLASS_TANNI"];
                                $data["指導要録情報"]["指導に関する記録1"][$cnt]["主として専門学科において開設される各教科_科目"][$count]["科目"]["修得単位数の計"] = $expRow["SUBCLASS_TANNI_TOTAL"];
                                if($expRow["SUBCLASS_BIKOU"] != ""){
                                    $data["指導要録情報"]["指導に関する記録1"][$cnt]["主として専門学科において開設される各教科_科目"][$count]["科目"]["備考"] = $expRow["SUBCLASS_BIKOU"];
                                }
                            }
                            if($expRow["SCHOOL_CLASS_NAME"] != ""){
                                $data["指導要録情報"]["指導に関する記録1"][$cnt]["主として専門学科において開設される各教科_科目"][$count]["学校設定教科"] = $expRow["SCHOOL_CLASS_NAME"];
                            }
                            if($expRow["SCHOOL_SUBCLASS_NAME"] != ""){
                                $data["指導要録情報"]["指導に関する記録1"][$cnt]["主として専門学科において開設される各教科_科目"][$count]["学校設定科目"]["学校設定科目名"] = $expRow["SCHOOL_SUBCLASS_NAME"];
                                $data["指導要録情報"]["指導に関する記録1"][$cnt]["主として専門学科において開設される各教科_科目"][$count]["学校設定科目"]["学校設定科目評定"] = $expRow["SCHOOL_SUBCLASS_HYOUTEI"];
                                $data["指導要録情報"]["指導に関する記録1"][$cnt]["主として専門学科において開設される各教科_科目"][$count]["学校設定科目"]["学校設定科目修得単位数"] = $expRow["SCHOOL_SUBCLASS_TANNI"];
                                $data["指導要録情報"]["指導に関する記録1"][$cnt]["主として専門学科において開設される各教科_科目"][$count]["学校設定科目"]["学校設定科目修得単位数の計"] = $expRow["SCHOOL_SUBCLASS_TANNI_TOTAL"];
                                if($expRow["SCHOOL_SUBCLASS_BIKOU"] != ""){
                                    $data["指導要録情報"]["指導に関する記録1"][$cnt]["主として専門学科において開設される各教科_科目"][$count]["学校設定科目"]["学校設定科目備考"] = $expRow["SCHOOL_SUBCLASS_BIKOU"];
                                }
                            }
                            
                            $count++;
                        }
                    }
                    
                    //留学
                    if($shiFrow["ABROAD_TANNI"] != ""){
                        $data["指導要録情報"]["指導に関する記録1"][$cnt]["留学"]["修得単位数"] = $shiFrow["ABROAD_TANNI"];
                    }
                    
                    //総合的な学習の時間の記録
                    if($shiFrow["INTEGRATED_TANNI"] != ""){
                        $data["指導要録情報"]["指導に関する記録1"][$cnt]["総合的な学習の時間の記録"]["修得単位数"] = $shiFrow["INTEGRATED_TANNI"];
                    }
                    if($shiFrow["INTEGRATED_ACTION"] != ""){
                        $data["指導要録情報"]["指導に関する記録1"][$cnt]["総合的な学習の時間の記録"]["学習活動"] = $shiFrow["INTEGRATED_ACTION"];
                    }
                    if($shiFrow["INTEGRATED_HYOUKA"] != ""){
                        $data["指導要録情報"]["指導に関する記録1"][$cnt]["総合的な学習の時間の記録"]["評価"] = $shiFrow["INTEGRATED_HYOUKA"];
                    }
                    //特別活動
                    if($shiFrow["SPECIAL_ACTION"] != ""){
                        $data["指導要録情報"]["指導に関する記録1"][$cnt]["特別活動の記録"]["活動の状況"] = $shiFrow["SPECIAL_ACTION"];
                    }
                    //自立活動の記録
                    if($shiFrow["ZIRITU_ACTION"] != ""){
                        $data["指導要録情報"]["指導に関する記録1"][$cnt]["自立活動の記録"]["自立の状況"] = $shiFrow["ZIRITU_ACTION"];
                    }
                    //総合所見及び指導上参考となる諸事項
                    if($shiFrow["SYOKEN"] != ""){
                        $data["指導要録情報"]["指導に関する記録1"][$cnt]["総合所見及び指導上参考となる諸事項"]["事項_事実_所見"] = $shiFrow["SYOKEN"];
                    }
                    //出欠の記録
                    $data["指導要録情報"]["指導に関する記録1"][$cnt]["出欠の記録"]["授業日数"] = $shiFrow["CLASS_CNT"];
                    $data["指導要録情報"]["指導に関する記録1"][$cnt]["出欠の記録"]["出席停止_忌引等の日数"] = $shiFrow["STOP_CNT"];
                    $data["指導要録情報"]["指導に関する記録1"][$cnt]["出欠の記録"]["留学中の授業日数"] = $shiFrow["ABROAD_CNT"];
                    if($shiFrow["MUST_CNT"] != ""){
                        $data["指導要録情報"]["指導に関する記録1"][$cnt]["出欠の記録"]["出席しなければならない日数"] = $shiFrow["MUST_CNT"];
                    }
                    
                    $data["指導要録情報"]["指導に関する記録1"][$cnt]["出欠の記録"]["欠席日数"] = $shiFrow["ABSENCE_CNT"];
                    $data["指導要録情報"]["指導に関する記録1"][$cnt]["出欠の記録"]["出席日数"] = $shiFrow["ATTEND_CNT"];
                    if($shiFrow["BIKOU"] != ""){
                        $data["指導要録情報"]["指導に関する記録1"][$cnt]["出欠の記録"]["備考"] = $shiFrow["BIKOU"];
                    }
                    
                    $cnt++;
                }
            }
        }else{
            //指導要録情報＞指導に関する記録2($this->SYUBETU = 2のときのみ)
            $query = knjo103Query::getShidou2($this->SCHREGNO, "1");
            $shiScnt = $db->getOne($query);
            
            if($shiScnt > 0){
                $query = knjo103Query::getShidou2($this->SCHREGNO);
                $result = $db->query($query);
                
                $cnt = 0;
                
                while($shiSrow = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                    $data["指導要録情報"]["指導に関する記録2"][$cnt]["様式種別"] = $shiSrow["SYUBETU"];
                    $data["指導要録情報"]["指導に関する記録2"][$cnt]["学年"] = $shiSrow["GRADE"];
                    if($shiSrow["STAY_CNT"] != ""){
                        $data["指導要録情報"]["指導に関する記録2"][$cnt]["原級留置回数"] = $shiSrow["STAY_CNT"];
                    }
                    if($shiSrow["RECORD"] != "" || $shiSrow["TOTAL_CLASS_CNT"] != ""){
                        $data["指導要録情報"]["指導に関する記録2"][$cnt]["各教科_特別活動_自立活動の記録"]["記録"] = $shiSrow["RECORD"];
                        $data["指導要録情報"]["指導に関する記録2"][$cnt]["各教科_特別活動_自立活動の記録"]["総授業時数"] = $shiSrow["TOTAL_CLASS_CNT"];
                    }
                    if($shiSrow["ACTION"] != ""){
                        $data["指導要録情報"]["指導に関する記録2"][$cnt]["総合的な学習の時間の記録"]["学習活動"] = $shiSrow["ACTION"];
                    }
                    if($shiSrow["HYOUKA"] != ""){
                        $data["指導要録情報"]["指導に関する記録2"][$cnt]["総合的な学習の時間の記録"]["評価"] = $shiSrow["HYOUKA"];
                    }
                    if($shiSrow["SYOKEN"] != ""){
                        $data["指導要録情報"]["指導に関する記録2"][$cnt]["総合所見及び指導上参考となる諸事項"]["事項_事実_所見"] = $shiSrow["SYOKEN"];
                    }
                    $data["指導要録情報"]["指導に関する記録2"][$cnt]["出欠の記録"]["授業日数"] = $shiSrow["CLASS_CNT"];
                    $data["指導要録情報"]["指導に関する記録2"][$cnt]["出欠の記録"]["出席停止_忌引等の日数"] = $shiSrow["STOP_CNT"];
                    $data["指導要録情報"]["指導に関する記録2"][$cnt]["出欠の記録"]["留学中の授業日数"] = $shiSrow["ABROAD_CLASS_CNT"];
                    if($shiSrow["MUST_CNT"] != ""){
                        $data["指導要録情報"]["指導に関する記録2"][$cnt]["出欠の記録"]["出席しなければならない日数"] = $shiSrow["MUST_CNT"];
                    }
                    $data["指導要録情報"]["指導に関する記録2"][$cnt]["出欠の記録"]["欠席日数"] = $shiSrow["ABSENCE_CNT"];
                    $data["指導要録情報"]["指導に関する記録2"][$cnt]["出欠の記録"]["出席日数"] = $shiSrow["ATTEND_CNT"];
                    if($shiSrow["BIKOU"] != ""){
                        $data["指導要録情報"]["指導に関する記録2"][$cnt]["出欠の記録"]["備考"] = $shiSrow["BIKOU"];
                    }
                    
                    
                    $cnt++;
                }
            }
        }
        
        //送信日時
        /*$query = knjo103Query::getSendDate($this->SCHREGNO);
        $send = $db->getOne($query);
        $sendex = explode(".", $send);
        
        $senddate = str_replace(" ", "T", $sendex[0]);
        $data["送信日時"] = $senddate."+09:00";*/
        $data["送信日時"] = date("c");

        
        //ファイル用に学校名取得
        $query =  knjo103Query::getSchoolName();
        $this->schoolName = $db->getOne($query);
        
        //DB切断
        Query::dbCheckIn($db);



        //初期値
        $count = 0;
        $past = "message";
        
        //functionに送る
        knjo103Model::create($data, $count, $past, $this);

        
        /*new dBug($this->new);
        exit;*/

        //タイトル部分
        $elementName = "指導要録情報メッセージ";
        $attribute = array("xmlns" => "urn:applic:xmlns:pf:edu:schema:2016-01",
                           "xmlns:xsi" => "http://www.w3.org/2001/XMLSchema-instance",
                           "xmlns:lgxml" => "urn:applic:xmlns:pf:lgxml:schema:2016-01",
                           "xsi:schemaLocation" => "urn:applic:xmlns:pf:edu:schema:2016-01 eduAK3101s-2016-01.xsd");


        //出力
        $today = date("Ymd");
        $time = date("His");
        $fileName = $this->schoolName."_".$this->SCHREGNO."_ST".$today.$time.'.xml';  //  ファイル名
         
        $this->dom = new DomDocument('1.0');  // DOMを作成
        $this->dom->encoding = "UTF-8"; // 文字コードをUTF-8に
        $this->dom->formatOutput = true; // 出力XMLを整形(改行,タブ)する
        
        
        $this->message = $this->dom->appendChild($this->dom->createElement($elementName));
        foreach($attribute as $key => $val){
            $this->message->setAttribute($key, $val);
        }
        

        for($count = 1; $count<count($this->new)+1; $count++){
            knjo103Model::makeXML($this->new[$count], $this); 
        }
        
        
        // HTTPヘッダを出力して
        header("Content-disposition: attachment; filename=".mb_convert_encoding($fileName, 'SJIS-win', 'UTF-8'));
        header("Content-type: application/octet-stream; name=" . $fileName);
         
        // DomXMLをXML形式で出力
        echo $this->dom->saveXML();

        $flg = true;
        //Query::dbCheckIn($db);

        if ($flg) {
            return true;
        } else {
            $this->setWarning("MSG303");    //データは存在していません。
            return false;
        }
        
    }




    function makeXML($array, &$make) {
        foreach($array as $key => $val){
            if(is_array($val)){
                foreach($val as $key2 => $val2){
                    $make->{$key2} = $make->{$key}->appendChild($make->dom->createElement("{$val2}"));
                }
            }else{
                $make->{$key}->appendChild($make->dom->createTextNode($array[$key]));
            }
        }
        
    }

    function create(&$array, $count, $past, &$data){
        $count++;
        
        foreach($array as $key => $val){
            $i = count($data->new[$count]);
            
            if(is_numeric($key)){
                
                if($key != "0"){
                    //ひとつ上の親になる部分の配列増やしたい
                    //$pastの後ろの数字が何文字かカウントする
                    $numCnt = 0;
                    for($j = 1; $j<mb_strlen($past); $j++){
                        $a = -1*$j;
                        $back = mb_substr($past, $a, 1);
                        if(is_numeric($back)){
                            $numCnt++;
                        }else{
                            break;
                        }
                    }
                    
                    
                    //$pastの文字列を数字で区切る
                    $parent = preg_split("/[0-9]/", $past);
                    
                    $parent = array_filter($parent);
                    $parent = array_values($parent);
                    
                    //後ろから2番目の配列の中身の文字数を数える
                    $mojicnt = mb_strlen($parent[count($parent)-1]);
                    
                    //数えた文字数+1文字分後ろから削除した文字列を作成する
                    $parname = mb_substr($past, 0, mb_strlen($past)-$mojicnt-$numCnt);
                    
                    //配列のキーに数字を追加して被らないようなキーにする
                    $now = $past.$key;
                    
                    //親になるキーを作る
                    $data->new[$count-1][$parname][$now] = $data->new[$count-1][$parname][$past];
                    
                }else{
                    $now = $past;
                }
                
                //$count--;
            }else{
                $now = $past.$key.$i;
                $data->new[$count][$past][$now] = $key;
                
            }
            
            if(is_array($val)){
                knjo103Model::create($val, $count, $now, $data);
            }else{
                $count++;
                $i = count($data->new[$count]);
                
                $data->new[$count][$now] = $val;
                
                $count--;
            }
        }
        return;
    }


    function getProperties()
    {
        $this->properties = array();
        //初期値
        $this->properties["useSchregRegdHdat"]  = "";

        $arr_useUnAdminMenuPrgid = array();
        $arr_useSubMenuId = array();
        $retVal = "";
    
        /*
         * configディレクトリ確認
         */
        if (file_exists(CONFDIR ."/prgInfo.properties")) {
            $filename = CONFDIR ."/prgInfo.properties";
        } else {
            $filename = DOCUMENTROOT ."/prgInfo.properties";
        }

        $fp = @fopen($filename, 'r');
        while ($line = fgets($fp,1024)) {
            foreach ($this->properties as $key => $value) {
                $pos = strpos($line, $key);
                if ($pos === false) {
                } else {
                    $retVal = str_replace($key." = ", "", $line);
                    $this->properties[$key] = str_replace("\r\n", "", $retVal);
                    if ($key == "useUnAdminMenuPrgid") {
                        $arr_useUnAdminMenuPrgid[] = str_replace("\r\n", "", $retVal);
                    }
                    if ($key == "useSubMenuId") {
                        $arr_useSubMenuId[] = str_replace("\r\n", "", $retVal);
                    }
                }
            }
        }
        fclose($fp);

        return $retVal;
    }
    


}

?>
