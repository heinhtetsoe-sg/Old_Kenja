<?php
//require_once('dBug.php');
require_once('for_php7.php');

class knjo123Model extends Model {



    var $grade;
    var $cmd;
    var $examyear;
    var $recordTableDiv;
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限


    function knjo123Model()
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

    public function schregChk($schregno)
    {
        $row = knjo123Query::getSchregregdData($this);
        if (!isset($row)) {
            $this->setWarning("", "該当する学籍番号はありません。");
            return false;
        }
        return true;
    }

    function updateModel()
    {
        $flg = false;
        
        //プロパティファイル読み込み
        $this->getProperties();

        //DB接続
        $db = Query::dbCheckOut();
        
        //データがあるかどうかチェック
        $query = knjo123Query::countData($this->SCHREGNO);
        $count = $db->getOne($query);
        if($count == 0){
            $this->setMessage("対象データがありません");
            return false;
        }
        
        $data = array();
        
        //データ作成
        $data["利用業務ユニット"] = "AK01";
        
        //送信先
        $query = knjo123Query::getToSchool($this->SCHREGNO, "1");
        $cnt = $db->getOne($query);
        if($cnt > 0){
            $query = knjo123Query::getToSchool($this->SCHREGNO);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            
            if($row["INFO_FLG"] != "2"){    //教育委員会タグを作るかどうか
                if($row["ID"] != ""){
                    $data["送信先情報"]["教育委員会"]["教育委員会ID"] = $row["ID"];
                }
                $data["送信先情報"]["教育委員会"]["外字教育委員会名"] = $row["GNAME"];
                $data["送信先情報"]["教育委員会"]["内字教育委員会名"] = $row["NNAME"];
                if($row["SECTION"] != ""){
                    $data["送信先情報"]["教育委員会"]["部課係"] = $row["SECTION"];
                }
                if($row["NADDR"] != ""){
                    $data["送信先情報"]["教育委員会"]["所在地"]["lgxml:住所コード"] = $row["ADDRCD"];
                    $data["送信先情報"]["教育委員会"]["所在地"]["lgxml:外字住所"] = $row["GADDR"];
                    $data["送信先情報"]["教育委員会"]["所在地"]["lgxml:内字住所"] = $row["NADDR"];
                    $data["送信先情報"]["教育委員会"]["所在地"]["lgxml:外字方書"] = $row["GKATAGAKI"];
                    $data["送信先情報"]["教育委員会"]["所在地"]["lgxml:内字方書"] = $row["NKATAGAKI"];
                    $data["送信先情報"]["教育委員会"]["所在地"]["lgxml:郵便番号"] = $row["ZIPCD"];
                }
                if($row["TELNO"] != ""){
                    $data["送信先情報"]["教育委員会"]["電話番号"] = $row["TELNO"];
                }
                if($row["SYOKU"] != ""){
                    $data["送信先情報"]["教育委員会"]["職"] = $row["SYOKU"];
                }
            }else{
                if($row["ID"] != ""){
                    $data["送信先情報"]["学校"]["学校ID"] = $row["ID"];
                }
                $data["送信先情報"]["学校"]["外字学校名"] = $row["GNAME"];
                $data["送信先情報"]["学校"]["内字学校名"] = $row["NNAME"];
                $data["送信先情報"]["学校"]["所在地"]["lgxml:住所コード"] = $row["ADDRCD"];
                $data["送信先情報"]["学校"]["所在地"]["lgxml:外字住所"] = $row["GADDR"];
                $data["送信先情報"]["学校"]["所在地"]["lgxml:内字住所"] = $row["NADDR"];
                $data["送信先情報"]["学校"]["所在地"]["lgxml:外字方書"] = $row["GKATAGAKI"];
                $data["送信先情報"]["学校"]["所在地"]["lgxml:内字方書"] = $row["NKATAGAKI"];
                $data["送信先情報"]["学校"]["所在地"]["lgxml:郵便番号"] = $row["ZIPCD"];
                if($row["BUN_ID"] != ""){
                    $data["送信先情報"]["学校"]["分校ID"] = $row["BUN_ID"];
                }
                if($row["BUN_GNAME"] != ""){
                    $data["送信先情報"]["学校"]["外字分校名"] = $row["BUN_GNAME"];
                }
                if($row["BUN_NNAME"] != ""){
                    $data["送信先情報"]["学校"]["内字分校名"] = $row["BUN_NNAME"];
                }
                if($row["BUN_NADDR"] != ""){
                    $data["送信先情報"]["学校"]["分校所在地"]["lgxml:住所コード"] = $row["BUN_ADDRCD"];
                    $data["送信先情報"]["学校"]["分校所在地"]["lgxml:外字住所"] = $row["BUN_GADDR"];
                    $data["送信先情報"]["学校"]["分校所在地"]["lgxml:内字住所"] = $row["BUN_NADDR"];
                    $data["送信先情報"]["学校"]["分校所在地"]["lgxml:外字方書"] = $row["BUN_GKATAGAKI"];
                    $data["送信先情報"]["学校"]["分校所在地"]["lgxml:内字方書"] = $row["BUN_NKATAGAKI"];
                    $data["送信先情報"]["学校"]["分校所在地"]["lgxml:郵便番号"] = $row["BUN_ZIPCD"];
                }
                if($row["TELNO"] != ""){
                    $data["送信先情報"]["学校"]["電話番号"] = $row["TELNO"];
                }
                if($row["SYOKU"] != ""){
                    $data["送信先情報"]["学校"]["職"] = $row["SYOKU"];
                }
                if($row["BIKOU"] != ""){
                    $data["送信先情報"]["学校"]["備考"] = $row["BIKOU"];
                }
            }
        }else{
            $data["送信先情報"] = "";
        }
        
        //送信元
        $query = knjo123Query::getFromSchool($this->SCHREGNO, "1");
        $cnt = $db->getOne($query);
        if($cnt > 0){
            $query = knjo123Query::getFromSchool($this->SCHREGNO);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            
            if($row["INFO_FLG"] != "2"){    //教育委員会タグを作るかどうか
                if($row["ID"] != ""){
                    $data["送信元情報"]["教育委員会"]["教育委員会ID"] = $row["ID"];
                }
                $data["送信元情報"]["教育委員会"]["外字教育委員会名"] = $row["GNAME"];
                $data["送信元情報"]["教育委員会"]["内字教育委員会名"] = $row["NNAME"];
                if($row["SECTION"] != ""){
                    $data["送信元情報"]["教育委員会"]["部課係"] = $row["SECTION"];
                }
                if($row["NADDR"] != ""){
                    $data["送信元情報"]["教育委員会"]["所在地"]["lgxml:住所コード"] = $row["ADDRCD"];
                    $data["送信元情報"]["教育委員会"]["所在地"]["lgxml:外字住所"] = $row["GADDR"];
                    $data["送信元情報"]["教育委員会"]["所在地"]["lgxml:内字住所"] = $row["NADDR"];
                    $data["送信元情報"]["教育委員会"]["所在地"]["lgxml:外字方書"] = $row["GKATAGAKI"];
                    $data["送信元情報"]["教育委員会"]["所在地"]["lgxml:内字方書"] = $row["NKATAGAKI"];
                    $data["送信元情報"]["教育委員会"]["所在地"]["lgxml:郵便番号"] = $row["ZIPCD"];
                }
                if($row["TELNO"] != ""){
                    $data["送信元情報"]["教育委員会"]["電話番号"] = $row["TELNO"];
                }
                if($row["SYOKU"] != ""){
                    $data["送信元情報"]["教育委員会"]["職"] = $row["SYOKU"];
                }
            }else{
                if($row["ID"] != ""){
                    $data["送信元情報"]["学校"]["学校ID"] = $row["ID"];
                }
                $data["送信元情報"]["学校"]["外字学校名"] = $row["GNAME"];
                $data["送信元情報"]["学校"]["内字学校名"] = $row["NNAME"];
                $data["送信元情報"]["学校"]["所在地"]["lgxml:住所コード"] = $row["ADDRCD"];
                $data["送信元情報"]["学校"]["所在地"]["lgxml:外字住所"] = $row["GADDR"];
                $data["送信元情報"]["学校"]["所在地"]["lgxml:内字住所"] = $row["NADDR"];
                $data["送信元情報"]["学校"]["所在地"]["lgxml:外字方書"] = $row["GKATAGAKI"];
                $data["送信元情報"]["学校"]["所在地"]["lgxml:内字方書"] = $row["NKATAGAKI"];
                $data["送信元情報"]["学校"]["所在地"]["lgxml:郵便番号"] = $row["ZIPCD"];
                if($row["BUN_ID"] != ""){
                    $data["送信元情報"]["学校"]["分校ID"] = $row["BUN_ID"];
                }
                if($row["BUN_GNAME"] != ""){
                    $data["送信元情報"]["学校"]["外字分校名"] = $row["BUN_GNAME"];
                }
                if($row["BUN_NNAME"] != ""){
                    $data["送信元情報"]["学校"]["内字分校名"] = $row["BUN_NNAME"];
                }
                if($row["BUN_NADDR"] != ""){
                    $data["送信元情報"]["学校"]["分校所在地"]["lgxml:住所コード"] = $row["BUN_ADDRCD"];
                    $data["送信元情報"]["学校"]["分校所在地"]["lgxml:外字住所"] = $row["BUN_GADDR"];
                    $data["送信元情報"]["学校"]["分校所在地"]["lgxml:内字住所"] = $row["BUN_NADDR"];
                    $data["送信元情報"]["学校"]["分校所在地"]["lgxml:外字方書"] = $row["BUN_GKATAGAKI"];
                    $data["送信元情報"]["学校"]["分校所在地"]["lgxml:内字方書"] = $row["BUN_NKATAGAKI"];
                    $data["送信元情報"]["学校"]["分校所在地"]["lgxml:郵便番号"] = $row["BUN_ZIPCD"];
                }
                if($row["TELNO"] != ""){
                    $data["送信元情報"]["学校"]["電話番号"] = $row["TELNO"];
                }
                if($row["SYOKU"] != ""){
                    $data["送信元情報"]["学校"]["職"] = $row["SYOKU"];
                }
                if($row["BIKOU"] != ""){
                    $data["送信元情報"]["学校"]["備考"] = $row["BIKOU"];
                }
            }
        }else{
            $data["送信元情報"] = "";
        }
        
        //指導要録情報
        $query = knjo123Query::getSyubetu($this->SCHREGNO);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        
        //指導要録種別
        $data["指導要録情報"]["指導要録種別"] = $row["SYUBETU"];
        if($row["SYUBETU"] == "P01"){   //児童指導要録
            $this->SYUBETU = 0;
        }else if($row["SYUBETU"] == "J01"){  //生徒指導要録
            $this->SYUBETU = 1;
        }
        
        //指導要録情報＞学校変更状況
        $query = knjo123Query::getSchChange($this->SCHREGNO);
        $result = $db->query($query);
        
        $cnt = 0;
        while($schChange = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $data["指導要録情報"]["学校変更状況"][$cnt]["年度"] = $schChange["YEAR"];
            $data["指導要録情報"]["学校変更状況"][$cnt]["学年"] = $schChange["GRADE"];
            
            //校長氏名
            $query = knjo123Query::getPrincipal($this->SCHREGNO, $schChange["DATA_ROW"]);
            $presult = $db->query($query);
            $priCnt = 0;
            while($priRow = $presult->fetchRow(DB_FETCHMODE_ASSOC)){
                $data["指導要録情報"]["学校変更状況"][$cnt]["校長氏名"][$priCnt]["lgxml:外字氏名"] = $priRow["GNAME"];
                $data["指導要録情報"]["学校変更状況"][$cnt]["校長氏名"][$priCnt]["lgxml:内字氏名"] = $priRow["NNAME"];
                $data["指導要録情報"]["学校変更状況"][$cnt]["校長氏名"][$priCnt]["lgxml:フリガナ"] = $priRow["NAME_KANA"];
                
                $priCnt++;
            }
            //学級担任者氏名
            $query = knjo123Query::getHrteacher($this->SCHREGNO, $schChange["DATA_ROW"]);
            $hresult = $db->query($query);
            $hrCnt = 0;
            while($hrRow = $hresult->fetchRow(DB_FETCHMODE_ASSOC)){
                $data["指導要録情報"]["学校変更状況"][$cnt]["学級担任者氏名"][$hrCnt]["lgxml:外字氏名"] = $hrRow["GNAME"];
                $data["指導要録情報"]["学校変更状況"][$cnt]["学級担任者氏名"][$hrCnt]["lgxml:内字氏名"] = $hrRow["NNAME"];
                $data["指導要録情報"]["学校変更状況"][$cnt]["学級担任者氏名"][$hrCnt]["lgxml:フリガナ"] = $hrRow["NAME_KANA"];
                
                $hrCnt++;
            }
            $data["指導要録情報"]["学校変更状況"][$cnt]["参考様式_版"] = $schChange["STYLE_VER"];
            
            $cnt++;
        }
        
        //指導要録情報＞学籍に関する記録
        if($row["STUDENT_ID"] != ""){
            $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["児童生徒ID"] = $row["STUDENT_ID"];
        }
        $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["氏名"]["lgxml:外字氏名"] = $row["GNAME"];
        $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["氏名"]["lgxml:内字氏名"] = $row["NNAME"];
        $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["氏名"]["lgxml:フリガナ"] = $row["NAME_KANA"];
        
        //指導要録情報＞学籍に関する記録＞児童生徒＞通称名
        $query = knjo123Query::getAlias($this->SCHREGNO, "1");
        $aliasCnt = $db->getOne($query);
        if($aliasCnt > 0){
            $query = knjo123Query::getAlias($this->SCHREGNO);
            $result = $db->query($query);
            $cnt = 0;
            while($aliasRow = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["通称名"][$cnt]["lgxml:外字氏名"] = $aliasRow["GNAME"];
                $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["通称名"][$cnt]["lgxml:内字氏名"] = $aliasRow["NNAME"];
                $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["通称名"][$cnt]["lgxml:フリガナ"] = $aliasRow["NAME_KANA"];
                
                $cnt++;
            }
        }
        
        //指導要録情報＞学籍に関する記録＞児童生徒＞
        $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["性別"] = $row["SEX"];
        $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["生年月日"] = $row["BIRTHDAY"];
        
        if($row["NADDR"] != ""){
            $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["現住所"]["住所"]["lgxml:住所コード"] = $row["ADDRCD"];
            $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["現住所"]["住所"]["lgxml:外字住所"] = $row["GADDR"];
            $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["現住所"]["住所"]["lgxml:内字住所"] = $row["NADDR"];
            $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["現住所"]["住所"]["lgxml:外字方書"] = $row["GKATAGAKI"];
            $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["現住所"]["住所"]["lgxml:内字方書"] = $row["NKATAGAKI"];
            $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["現住所"]["住所"]["lgxml:郵便番号"] = $row["ZIPCD"];
            if($row["ADDR_BIKOU"] != ""){
                $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["現住所"]["備考"] = $row["ADDR_BIKOU"];
            }
        }
        
        //指導要録情報＞学籍に関する記録＞児童生徒＞現住所_その他
        $query = knjo123Query::getStOtherAdd($this->SCHREGNO, "1");
        $addCnt = $db->getOne($query);
        if($addCnt > 0){
            $query = knjo123Query::getStOtherAdd($this->SCHREGNO);
            $result = $db->query($query);
            $cnt = 0;
            
            while($otrAdd = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["現住所_その他"][$cnt]["住所"]["lgxml:住所コード"] = $otrAdd["ADDRCD"];
                $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["現住所_その他"][$cnt]["住所"]["lgxml:外字住所"] = $otrAdd["GADDR"];
                $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["現住所_その他"][$cnt]["住所"]["lgxml:内字住所"] = $otrAdd["NADDR"];
                $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["現住所_その他"][$cnt]["住所"]["lgxml:外字方書"] = $otrAdd["GKATAGAKI"];
                $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["現住所_その他"][$cnt]["住所"]["lgxml:内字方書"] = $otrAdd["NKATAGAKI"];
                $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["現住所_その他"][$cnt]["住所"]["lgxml:郵便番号"] = $otrAdd["ZIPCD"];
                if($otrAdd["BIKOU"] != ""){
                    $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["現住所_その他"][$cnt]["備考"] = $otrAdd["BIKOU"];
                }
                
                $cnt++;
            }
        }
        
        //指導要録情報＞学籍に関する記録＞生徒＞国籍
        if($row["NATION"] != ""){
            $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["国籍"] = $row["NATION"];
        }
        
        //指導要録情報＞学籍に関する記録＞児童生徒＞保護者
        $query = knjo123Query::getParent($this->SCHREGNO);
        $result = $db->query($query);
        
        $count = 0;
        
        while($parentRow = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            if($parentRow["ID"] != ""){
                $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["保護者"][$count]["保護者ID"] = $parentRow["ID"];
            }
            $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["保護者"][$count]["氏名"]["lgxml:外字氏名"] = $parentRow["GNAME"];
            $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["保護者"][$count]["氏名"]["lgxml:内字氏名"] = $parentRow["NNAME"];
            $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["保護者"][$count]["氏名"]["lgxml:フリガナ"] = $parentRow["NAME_KANA"];
            $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["保護者"][$count]["現住所"]["住所"]["lgxml:住所コード"] = $parentRow["ADDRCD"];
            $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["保護者"][$count]["現住所"]["住所"]["lgxml:外字住所"] = $parentRow["GADDR"];
            $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["保護者"][$count]["現住所"]["住所"]["lgxml:内字住所"] = $parentRow["NADDR"];
            $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["保護者"][$count]["現住所"]["住所"]["lgxml:外字方書"] = $parentRow["GKATAGAKI"];
            $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["保護者"][$count]["現住所"]["住所"]["lgxml:内字方書"] = $parentRow["NKATAGAKI"];
            $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["保護者"][$count]["現住所"]["住所"]["lgxml:郵便番号"] = $parentRow["ZIPCD"];
            if($parentRow["BIKOU"] != ""){
                $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["保護者"][$count]["現住所"]["備考"] = $parentRow["BIKOU"];
            }
            
            //保護者＞現住所_その他
            $query = knjo123Query::getParentOtrAdd($this->SCHREGNO, $parentRow["DATA_ROW"], "1");
            $pOtrCnt = $db->getOne($query);
            if($pOtrCnt > 0){
                $query = knjo123Query::getParentOtrAdd($this->SCHREGNO, $parentRow["DATA_ROW"]);
                $oresult = $db->query($query);
                $poCnt = 0;
                while($poRow = $oresult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["保護者"][$count]["現住所_その他"][$poCnt]["住所"]["lgxml:住所コード"] = $poRow["ADDRCD"];
                    $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["保護者"][$count]["現住所_その他"][$poCnt]["住所"]["lgxml:外字住所"] = $poRow["GADDR"];
                    $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["保護者"][$count]["現住所_その他"][$poCnt]["住所"]["lgxml:内字住所"] = $poRow["NADDR"];
                    $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["保護者"][$count]["現住所_その他"][$poCnt]["住所"]["lgxml:外字方書"] = $poRow["GKATAGAKI"];
                    $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["保護者"][$count]["現住所_その他"][$poCnt]["住所"]["lgxml:内字方書"] = $poRow["NKATAGAKI"];
                    $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["保護者"][$count]["現住所_その他"][$poCnt]["住所"]["lgxml:郵便番号"] = $poRow["ZIPCD"];
                    if($poRow["BIKOU"] != ""){
                        $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["保護者"][$count]["現住所_その他"][$poCnt]["備考"] = $poRow["BIKOU"];
                    }
                    
                    $poCnt++;
                }
                
            }
            
            $count++;
        }
        
        //指導要録情報＞学籍に関する記録＞在学状況
        if($row["SCHOOL_ID"] != ""){
            $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["学校情報"]["学校ID"] = $row["SCHOOL_ID"];
        }
        $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["学校情報"]["外字学校名"] = $row["SCHOOL_GNAME"];
        $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["学校情報"]["内字学校名"] = $row["SCHOOL_NNAME"];
        $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["学校情報"]["所在地"]["lgxml:住所コード"] = $row["SCHOOL_ADDRCD"];
        $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["学校情報"]["所在地"]["lgxml:外字住所"] = $row["SCHOOL_GADDR"];
        $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["学校情報"]["所在地"]["lgxml:内字住所"] = $row["SCHOOL_NADDR"];
        $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["学校情報"]["所在地"]["lgxml:外字方書"] = $row["SCHOOL_GKATAGAKI"];
        $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["学校情報"]["所在地"]["lgxml:内字方書"] = $row["SCHOOL_NKATAGAKI"];
        $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["学校情報"]["所在地"]["lgxml:郵便番号"] = $row["SCHOOL_ZIPCD"];
        if($row["SCHOOL_BUN_ID"] != ""){
            $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["学校情報"]["分校ID"] = $row["SCHOOL_BUN_ID"];
        }
        if($row["SCHOOL_BUN_GNAME"] != ""){
            $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["学校情報"]["外字分校名"] = $row["SCHOOL_BUN_GNAME"];
        }
        if($row["SCHOOL_BUN_NNAME"] != ""){
            $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["学校情報"]["内字分校名"] = $row["SCHOOL_BUN_NNAME"];
        }
        if($row["SCHOOL_BUN_NADDR"] != ""){
            $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["学校情報"]["分校所在地"]["lgxml:住所コード"] = $row["SCHOOL_BUN_ADDRCD"];
            $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["学校情報"]["分校所在地"]["lgxml:外字住所"] = $row["SCHOOL_BUN_GADDR"];
            $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["学校情報"]["分校所在地"]["lgxml:内字住所"] = $row["SCHOOL_BUN_NADDR"];
            $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["学校情報"]["分校所在地"]["lgxml:外字方書"] = $row["SCHOOL_BUN_GKATAGAKI"];
            $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["学校情報"]["分校所在地"]["lgxml:内字方書"] = $row["SCHOOL_BUN_NKATAGAKI"];
            $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["学校情報"]["分校所在地"]["lgxml:郵便番号"] = $row["SCHOOL_BUN_ZIPCD"];
        }
        if($row["SCHOOL_TELNO"] != ""){
            $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["学校情報"]["電話番号"] = $row["SCHOOL_TELNO"];
        }
        if($row["SCHOOL_SYOKU"] != ""){
            $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["学校情報"]["職"] = $row["SCHOOL_SYOKU"];
        }
        if($row["SCHOOL_BIKOU"] != ""){
            $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["学校情報"]["備考"] = $row["SCHOOL_BIKOU"];
        }
        
        
        //学籍に関する記録＞在学状況＞在学学年
        $query = knjo123Query::getZaigaku($this->SCHREGNO);
        $result = $db->query($query);
        
        $count = 0;
        
        while($zaiRow = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["在学学年"][$count]["年度"] = $zaiRow["YEAR"];
            $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["在学学年"][$count]["学年"] = $zaiRow["GRADE"];
            $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["在学学年"][$count]["学級"] = $zaiRow["HR_CLASS"];
            $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["在学学年"][$count]["整理番号"] = $zaiRow["ATTENDNO"];
            
            $count++;
        }
        
        //異動状況＞入学前の経歴
        $query = knjo123Query::getKeireki($this->SCHREGNO, "1");
        $krkCnt = $db->getOne($query);
        if($krkCnt > 0){
            $query = knjo123Query::getKeireki($this->SCHREGNO);
            $result = $db->query($query);
            
            $cnt = 0;
            
            while($krkRow = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $data["指導要録情報"]["学籍に関する記録"]["異動状況"]["入学前の経歴"][$cnt] = $krkRow["KEIREKI"];
                
                $cnt++;
            }
        }
        
        //指導要録情報＞学籍に関する記録＞異動状況
        $query = knjo123Query::getIdouMst($this->SCHREGNO);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        
        //入学or編入学or転入学
        if($row["IN_FLG"] == "1"){
            $top = "入学";
        }else if($row["IN_FLG"] == "2"){
            $top = "編入学";
        }else{
            $top = "転入学";
        }
        
        $data["指導要録情報"]["学籍に関する記録"]["異動状況"][$top][$top."年月日"] = $row["IN_DATE"];
        if($row["IN_GRADE"] != ""){
            $data["指導要録情報"]["学籍に関する記録"]["異動状況"][$top][$top."時学年"] = $row["IN_GRADE"];
        }
        if($row["IN_BIKOU"] != ""){
            $data["指導要録情報"]["学籍に関する記録"]["異動状況"][$top]["備考"] = $row["IN_BIKOU"];
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
            
            $data["指導要録情報"]["学籍に関する記録"]["異動状況"][$top][$top."年月日"] = $row["OUT_DATE"];
            if($row["LEAVE_DATE"] != ""){
                $data["指導要録情報"]["学籍に関する記録"]["異動状況"][$top]["学校を去った日"] = $row["LEAVE_DATE"];
            }
            if($row["OUT_BIKOU"] != ""){
                $data["指導要録情報"]["学籍に関する記録"]["異動状況"][$top]["備考"] = $row["OUT_BIKOU"];
            }
        }
        
        //進学先就職先等
        if($row["AFTER_LEAVE"] != ""){
            $data["指導要録情報"]["学籍に関する記録"]["異動状況"]["進学先就職先等"] = $row["AFTER_LEAVE"];
        }
        
        //学籍に関する記録＞備考
        if($row["BIKOU"] != ""){
            $data["指導要録情報"]["学籍に関する記録"]["備考"] = $row["BIKOU"];
        }
        
        //指導要録情報＞指導に関する記録
        $query = knjo123Query::getShidou($this->SCHREGNO);
        $shiFresult = $db->query($query);
        
        $cnt = 0;
        
        while($shiFrow = $shiFresult->fetchRow(DB_FETCHMODE_ASSOC)){
            $data["指導要録情報"]["指導に関する記録"][$cnt]["学年"] = $shiFrow["GRADE"];
            $data["指導要録情報"]["指導に関する記録"][$cnt]["様式種別"] = $shiFrow["SYUBETU"];
            
            if($shiFrow["SYUBETU"] == "01"){    //健常
                $this->youSyubetu = 0;
            }else if($shiFrow["SYUBETU"] == "02"){  //身体障害
                $this->youSyubetu = 1;
            }else{
                $this->youSyubetu = 2;      //知的障害
            }
            
            //各教科の学習の記録
            if($this->youSyubetu != 2){
                //観点別学習状況か評定情報のデータいずれかの存在をチェック
                $query = knjo123Query::getSubjCnt($this->SCHREGNO, $shiFrow["DATA_ROW"]);
                $subjCnt = $db->getOne($query);
                if($subjCnt > 0){
                    //観点別学習状況
                    $query = knjo123Query::getSubjPoint($this->SCHREGNO, $shiFrow["DATA_ROW"], "1");
                    $subjPcnt = $db->getOne($query);
                    
                    if($subjPcnt > 0){
                        $query = knjo123Query::getSubjPoint($this->SCHREGNO, $shiFrow["DATA_ROW"]);
                        $subjResult = $db->query($query);
                        $subCnt = 0;
                        
                        while($subjRow = $subjResult->fetchRow(DB_FETCHMODE_ASSOC)){
                            if($subjRow["MUST_CHOICE"] != ""){
                                $data["指導要録情報"]["指導に関する記録"][$cnt]["各教科の学習の記録"]["観点別学習状況"][$subCnt]["必修選択"] = $subjRow["MUST_CHOICE"];
                            }
                            if($subjRow["OTHER_SUBJECT"] != ""){
                                $data["指導要録情報"]["指導に関する記録"][$cnt]["各教科の学習の記録"]["観点別学習状況"][$subCnt]["教科_その他"] = $subjRow["OTHER_SUBJECT"];
                            }else{
                                $data["指導要録情報"]["指導に関する記録"][$cnt]["各教科の学習の記録"]["観点別学習状況"][$subCnt]["教科"] = $subjRow["SUBJECT"];
                            }
                            if($subjRow["OTHER_POINT"] != ""){
                                $data["指導要録情報"]["指導に関する記録"][$cnt]["各教科の学習の記録"]["観点別学習状況"][$subCnt]["観点_その他"] = $subjRow["OTHER_POINT"];
                            }else{
                                $data["指導要録情報"]["指導に関する記録"][$cnt]["各教科の学習の記録"]["観点別学習状況"][$subCnt]["観点"] = $subjRow["POINT"];
                            }
                            $data["指導要録情報"]["指導に関する記録"][$cnt]["各教科の学習の記録"]["観点別学習状況"][$subCnt]["評価"] = $subjRow["SCORE"];
                            
                            
                            $subCnt++;
                        }
                    }else{
                        //データなくてもタグは作らないといけない
                        $data["指導要録情報"]["指導に関する記録"][$cnt]["各教科の学習の記録"]["観点別学習状況"]["教科"] = "";
                        $data["指導要録情報"]["指導に関する記録"][$cnt]["各教科の学習の記録"]["観点別学習状況"]["観点"] = "";
                        $data["指導要録情報"]["指導に関する記録"][$cnt]["各教科の学習の記録"]["観点別学習状況"]["評価"] = "";
                    }
                    
                    //評定情報
                    $query = knjo123Query::getSubjScore($this->SCHREGNO, $shiFrow["DATA_ROW"], "1");
                    $subjScnt = $db->getOne($query);
                    
                    if($subjScnt > 0){
                        $query = knjo123Query::getSubjScore($this->SCHREGNO, $shiFrow["DATA_ROW"]);
                        $subjResult = $db->query($query);
                        
                        $subCnt = 0;
                        
                        while($subjRow = $subjResult->fetchRow(DB_FETCHMODE_ASSOC)){
                            if($subjRow["MUST_CHOICE"] != ""){
                                $data["指導要録情報"]["指導に関する記録"][$cnt]["各教科の学習の記録"]["評定情報"][$subCnt]["必修選択"] = $subjRow["MUST_CHOICE"];
                            }
                            if($subjRow["OTHER_SUBJECT"] != ""){
                                $data["指導要録情報"]["指導に関する記録"][$cnt]["各教科の学習の記録"]["評定情報"][$subCnt]["教科_その他"] = $subjRow["OTHER_SUBJECT"];
                            }else{
                                $data["指導要録情報"]["指導に関する記録"][$cnt]["各教科の学習の記録"]["評定情報"][$subCnt]["教科"] = $subjRow["SUBJECT"];
                            }
                            $data["指導要録情報"]["指導に関する記録"][$cnt]["各教科の学習の記録"]["評定情報"][$subCnt]["評定"] = $subjRow["SCORE"];
                            
                            $subCnt++;
                        }
                    }
                }
            }
            
            //特別の教科_道徳　様式種別のチェックは不要か。様式種別_版はチェックする。
            $array = array("F02", "F03");
            if(in_array($data["指導要録情報"]["学校変更状況"][$cnt]["参考様式_版"], $array)){
                //存在チェック
                $query = knjo123Query::getMoral($this->SCHREGNO, $shiFrow["DATA_ROW"]);
                $moralCnt = $db->getOne($query);
                if($moralCnt != ""){
                    $moralResult = $db->query($query);
                    while($moralRow = $moralResult->fetchRow(DB_FETCHMODE_ASSOC)){
                        $data["指導要録情報"]["指導に関する記録"][$cnt]["特別の教科_道徳"]["評価"] = $moralRow["POINT"];
                    }
                // }else{
                //     //データなくてもタグは作らないといけない
                //     $data["指導要録情報"]["指導に関する記録"][$cnt]["特別の教科_道徳"]["評価"] = "";
                }
            }


            //外国語活動の記録(児童で知的障害以外の場合のみ)
            if($this->SYUBETU == 0 && $this->youSyubetu != 2){
                $query = knjo123Query::getForLang($this->SCHREGNO, $shiFrow["DATA_ROW"], "1");
                $forCnt = $db->getOne($query);
                
                if($forCnt > 0){
                    $query = knjo123Query::getForLang($this->SCHREGNO, $shiFrow["DATA_ROW"]);
                    $forResult = $db->query($query);
                    
                    $forCnt = 0;
                    
                    while($forRow = $forResult->fetchRow(DB_FETCHMODE_ASSOC)){
                        if($forRow["OTHER_POINT"] != ""){
                            $data["指導要録情報"]["指導に関する記録"][$cnt]["外国語活動の記録"]["学習活動の状況"][$forCnt]["観点_その他"] = $forRow["OTHER_POINT"];
                            $data["指導要録情報"]["指導に関する記録"][$cnt]["外国語活動の記録"]["学習活動の状況"][$forCnt]["評価"] = $forRow["SCORE"];
                        }else if($forRow["POINT"] != ""){
                            $data["指導要録情報"]["指導に関する記録"][$cnt]["外国語活動の記録"]["学習活動の状況"][$forCnt]["観点"] = $forRow["POINT"];
                            $data["指導要録情報"]["指導に関する記録"][$cnt]["外国語活動の記録"]["学習活動の状況"][$forCnt]["評価"] = $forRow["SCORE"];
                        }else if($forRow["ONLY_SCORE"] != ""){
                            $data["指導要録情報"]["指導に関する記録"][$cnt]["外国語活動の記録"]["学習活動の状況"][$forCnt]["評価"] = $forRow["ONLY_SCORE"];
                        }
                        
                        $forCnt++;
                    }
                }
            }
            
            //総合的な学習の時間の記録
            if($this->youSyubetu != 2){
                $query = knjo123Query::getIntegrated($this->SCHREGNO, $shiFrow["DATA_ROW"], "1");
                $integratedCnt = $db->getOne($query);
                
                if($integratedCnt > 0){
                    $query = knjo123Query::getIntegrated($this->SCHREGNO, $shiFrow["DATA_ROW"]);
                    $integResult = $db->query($query);
                    
                    $integCnt = 0;
                    
                    while($integRow = $integResult->fetchRow(DB_FETCHMODE_ASSOC)){
                        if($integRow["ACTION"] != ""){
                            $data["指導要録情報"]["指導に関する記録"][$cnt]["総合的な学習の時間の記録"]["学習活動の状況"][$integCnt]["学習活動"] = $integRow["ACTION"];
                        }
                        //観点
                        $query = knjo123Query::getIntegPoint($this->SCHREGNO, $shiFrow["DATA_ROW"], $integRow["DATA_ROW"], "1");
                        $integPcnt = $db->getOne($query);
                        
                        if($integPcnt > 0){
                            $query = knjo123Query::getIntegPoint($this->SCHREGNO, $shiFrow["DATA_ROW"], $integRow["DATA_ROW"]);
                            $integPresult = $db->query($query);
                            
                            $integPointCnt = 0;
                            
                            while($integProw = $integPresult->fetchRow(DB_FETCHMODE_ASSOC)){
                                $data["指導要録情報"]["指導に関する記録"][$cnt]["総合的な学習の時間の記録"]["学習活動の状況"][$integCnt]["観点"][$integPointCnt] = $integProw["POINT"];
                                
                                $integPointCnt++;
                            }
                        }
                        
                        //評価
                        $data["指導要録情報"]["指導に関する記録"][$cnt]["総合的な学習の時間の記録"]["学習活動の状況"][$integCnt]["評価"] = $integRow["SCORE"];
                        
                        $integCnt++;
                    }
                }
            }
            
            //特別活動の記録
            if($this->youSyubetu != 2){
                $query = knjo123Query::getSpAct($this->SCHREGNO, $shiFrow["DATA_ROW"], "1");
                $spCnt = $db->getOne($query);
                if($spCnt > 0){
                    $query = knjo123Query::getSpAct($this->SCHREGNO, $shiFrow["DATA_ROW"]);
                    $spResult = $db->query($query);
                    
                    $spActCnt = 0;
                    
                    while($spRow = $spResult->fetchRow(DB_FETCHMODE_ASSOC)){
                        if($spRow["OTHER_ACTION"] != ""){
                            $data["指導要録情報"]["指導に関する記録"][$cnt]["特別活動の記録"]["活動の状況"][$spActCnt]["内容_その他"] = $spRow["OTHER_ACTION"];
                        }else{
                            $data["指導要録情報"]["指導に関する記録"][$cnt]["特別活動の記録"]["活動の状況"][$spActCnt]["内容"] = $spRow["ACTION"];
                        }
                        if($spRow["POINT"] != ""){
                            $data["指導要録情報"]["指導に関する記録"][$cnt]["特別活動の記録"]["活動の状況"][$spActCnt]["観点"] = $spRow["POINT"];
                        }
                        $data["指導要録情報"]["指導に関する記録"][$cnt]["特別活動の記録"]["活動の状況"][$spActCnt]["評価"] = $spRow["SCORE"];
                        
                        $spActCnt++;
                    }
                }
            }
            
            //行動の記録
            if($this->youSyubetu != 2){
                //行動の状況
                $query = knjo123Query::getBehave($this->SCHREGNO, $shiFrow["DATA_ROW"], "1");
                $behavCnt = $db->getOne($query);
                
                if($behavCnt > 0){
                    $query = knjo123Query::getBehave($this->SCHREGNO, $shiFrow["DATA_ROW"]);
                    $behavResult = $db->query($query);
                    
                    $beCnt = 0;
                    
                    while($behavRow = $behavResult->fetchRow(DB_FETCHMODE_ASSOC)){
                        if($behavRow["OTHER_ITEM"] != ""){
                            $data["指導要録情報"]["指導に関する記録"][$cnt]["行動の記録"]["行動の状況"][$beCnt]["項目_その他"] = $behavRow["OTHER_ITEM"];
                        }else{
                            $data["指導要録情報"]["指導に関する記録"][$cnt]["行動の記録"]["行動の状況"][$beCnt]["項目"] = $behavRow["ITEM"];
                        }
                        $data["指導要録情報"]["指導に関する記録"][$cnt]["行動の記録"]["行動の状況"][$beCnt]["評価"] = $behavRow["SCORE_CODE"];
                        
                        $beCnt++;
                    }
                }
            }else{
                //評価
                $query = knjo123Query::getBehave($this->SCHREGNO, $shiFrow["DATA_ROW"]);
                $behavRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $data["指導要録情報"]["指導に関する記録"][$cnt]["行動の記録"]["評価"] = $behavRow["SCORE"];
            }
            
            //自立活動の記録
            if($this->youSyubetu == 1){
                if($shiFrow["JIRITU_ACT"] != ""){
                    $data["指導要録情報"]["指導に関する記録"][$cnt]["自立活動の記録"]["自立の状況"] = $shiFrow["JIRITU_ACT"];
                }
            }
            
            //各教科_特別活動_自立活動の記録
            if($this->youSyubetu == 2){
                $query = knjo123Query::getAllAct($this->SCHREGNO, $shiFrow["DATA_ROW"], "1");
                $allActCnt = $db->getOne($query);
                
                if($allActCnt > 0){
                    $query = knjo123Query::getAllAct($this->SCHREGNO, $shiFrow["DATA_ROW"]);
                    $allResult = $db->query($query);
                    
                    $allCnt = 0;
                    
                    while($allRow = $allResult->fetchRow(DB_FETCHMODE_ASSOC)){
                        if($allRow["OTHER_SUBJECT"] != ""){
                            $data["指導要録情報"]["指導に関する記録"][$cnt]["各教科_特別活動_自立活動の記録"]["状況"][$allCnt]["教科等_その他"] = $allRow["OTHER_SUBJECT"];
                        }else{
                            $data["指導要録情報"]["指導に関する記録"][$cnt]["各教科_特別活動_自立活動の記録"]["状況"][$allCnt]["教科等"] = $allRow["SUBJECT"];
                        }
                        $data["指導要録情報"]["指導に関する記録"][$cnt]["各教科_特別活動_自立活動の記録"]["状況"][$allCnt]["評価"] = $allRow["SCORE"];
                        
                        $allCnt++;
                    }
                }
            }
            
            //総合的所見及び指導上参考となる諸事項
            if($shiFrow["GENERAL_FINDINGS"] != ""){
                $data["指導要録情報"]["指導に関する記録"][$cnt]["総合的所見及び指導上参考となる諸事項"]["事項"]["事項_事実_所見"] = $shiFrow["GENERAL_FINDINGS"];
            }
            
            //出欠の記録
            $data["指導要録情報"]["指導に関する記録"][$cnt]["出欠の記録"]["授業日数"] = $shiFrow["CLASS_CNT"];
            $data["指導要録情報"]["指導に関する記録"][$cnt]["出欠の記録"]["出席停止_忌引等の日数"] = $shiFrow["STOP_CNT"];
            if($shiFrow["MUST_CNT"] != ""){
                $data["指導要録情報"]["指導に関する記録"][$cnt]["出欠の記録"]["出席しなければならない日数"] = $shiFrow["MUST_CNT"];
            }
            $data["指導要録情報"]["指導に関する記録"][$cnt]["出欠の記録"]["欠席日数"] = $shiFrow["ABSENCE_CNT"];
            $data["指導要録情報"]["指導に関する記録"][$cnt]["出欠の記録"]["出席日数"] = $shiFrow["ATTEND_CNT"];
            if($shiFrow["BIKOU"] != ""){
                $data["指導要録情報"]["指導に関する記録"][$cnt]["出欠の記録"]["備考"] = $shiFrow["BIKOU"];
            }
            
            $cnt++;
        }
        
        
        //入学時の障害の状態
        if($this->youSyubetu != 0){
            if($row["HANDICAP"] != ""){
                $data["指導要録情報"]["入学時の障害の状態"]["障害の状態"] = $row["HANDICAP"];
            }
        }
        //送信日時
        /*$query = knjo123Query::getSendDate($this->SCHREGNO);
        $send = $db->getOne($query);
        $sendex = explode(".", $send);
        
        $senddate = str_replace(" ", "T", $sendex[0]);
        $data["送信日時"] = $senddate."+09:00";*/
        $data["送信日時"] = date("c");

        
        //ファイル用に学校名取得
        if($this->SYUBETU == 0) {
            $schoolKind ='P';
        } else if ($this->SYUBETU == 1){
            $schoolKind = 'J';
        }
        $query =  knjo123Query::getSchoolName($schoolKind);
        $this->schoolName = $db->getOne($query);
        
        //DB切断
        Query::dbCheckIn($db);



        //初期値
        $count = 0;
        $past = "message";
        
        //functionに送る
        knjo123Model::create($data, $count, $past, $this);

        
        /*new dBug($this->new);
        exit;*/

        //タイトル部分
        $elementName = "指導要録情報メッセージ";
        $attribute = array("xmlns" => "urn:applic:xmlns:pf:edu:schema:2020-01",
                           "xmlns:lgxml" => "urn:applic:xmlns:pf:lgxml:schema:2020-01",
                           "xmlns:xsi" => "http://www.w3.org/2001/XMLSchema-instance",
                           "xsi:schemaLocation" => "urn:applic:xmlns:pf:edu:schema:2020-01 eduAK0101s-2020-01.xsd");


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
        

        for($count = 1; $count<get_count($this->new)+1; $count++){
            knjo123Model::makeXML($this->new[$count], $this); 
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
            $i = get_count($data->new[$count]);
            
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
                    $mojicnt = mb_strlen($parent[get_count($parent)-1]);
                    
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
                knjo123Model::create($val, $count, $now, $data);
            }else{
                $count++;
                $i = get_count($data->new[$count]);
                
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
